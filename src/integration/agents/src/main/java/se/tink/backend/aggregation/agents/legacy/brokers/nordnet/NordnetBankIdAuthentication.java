package se.tink.backend.aggregation.agents.brokers.nordnet;

import static se.tink.backend.aggregation.agents.brokers.nordnet.NordnetConstants.Patterns.FIND_BANKID_URL;
import static se.tink.backend.aggregation.agents.brokers.nordnet.NordnetConstants.Patterns.FIND_SAMLART_FROM_URI;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import java.net.URI;
import java.net.URLDecoder;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import javax.ws.rs.core.MediaType;
import org.eclipse.jetty.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.aggregation.agents.bankid.status.BankIdStatus;
import se.tink.backend.aggregation.agents.brokers.nordnet.NordnetConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.brokers.nordnet.NordnetConstants.Patterns;
import se.tink.backend.aggregation.agents.brokers.nordnet.NordnetConstants.QueryParamValues;
import se.tink.backend.aggregation.agents.brokers.nordnet.NordnetConstants.Urls;
import se.tink.backend.aggregation.agents.brokers.nordnet.model.ErrorEntity;
import se.tink.backend.aggregation.agents.brokers.nordnet.model.LoginAnonymousPostResponse;
import se.tink.backend.aggregation.agents.brokers.nordnet.model.Request.CollectBankIdRequest;
import se.tink.backend.aggregation.agents.brokers.nordnet.model.Request.FetchTokenRequest;
import se.tink.backend.aggregation.agents.brokers.nordnet.model.Request.InitBankIdRequest;
import se.tink.backend.aggregation.agents.brokers.nordnet.model.Request.SAMLRequest;
import se.tink.backend.aggregation.agents.brokers.nordnet.model.Response.ArtifactResponse;
import se.tink.backend.aggregation.agents.brokers.nordnet.model.Response.BankIdInitSamlResponse;
import se.tink.backend.aggregation.agents.brokers.nordnet.model.Response.CollectBankIdResponse;
import se.tink.backend.aggregation.agents.brokers.nordnet.model.Response.InitBankIdResponse;
import se.tink.backend.aggregation.agents.brokers.nordnet.model.Response.TokenResponse;
import se.tink.backend.aggregation.agents.brokers.nordnet.model.html.CompleteBankIdPage;
import se.tink.backend.aggregation.agents.exceptions.BankIdException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.errors.BankIdError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;

public class NordnetBankIdAuthentication {

    private static final Logger log = LoggerFactory.getLogger(NordnetBankIdAuthentication.class);

    private final NordnetApiClient apiClient;
    private final Credentials credentials;
    private String bankIdUrl;

    public NordnetBankIdAuthentication(NordnetApiClient apiClient, Credentials credentials) {
        this.apiClient = apiClient;
        this.credentials = credentials;
    }

    public String initBankID(String username) throws BankIdException {
        loadLoginPage();
        BankIdInitSamlResponse bankIdInitSamlResponse =
                apiClient.get(Urls.LOGIN_BANKID_PAGE_URL, BankIdInitSamlResponse.class);

        String html = apiClient.get(bankIdInitSamlResponse.getRequestUrl(), String.class);
        Matcher matcher = FIND_BANKID_URL.matcher(html);

        Preconditions.checkState(matcher.find(), "Couldn't find url to initiate BankID");
        bankIdUrl = matcher.group();

        // Try to initiate the bankid auth twice (if the first one fails). It will fail the first
        // time if the customer
        // has an already active authentication (both authentications will be cancelled by bankid)
        InitBankIdResponse initBankIdResponse = null;

        for (int i = 0; i < 2; i++) {
            initBankIdResponse =
                    apiClient.post(
                            bankIdUrl + NordnetConstants.Urls.BANKID_ORDER_SUFFIX,
                            new InitBankIdRequest(username),
                            InitBankIdResponse.class);

            String orderRef = initBankIdResponse.getOrderRef();

            if (!Strings.isNullOrEmpty(orderRef)) {
                return orderRef;
            }
        }

        handleKnownBankIdInitError(initBankIdResponse);

        throw new IllegalStateException("Missing BankID order ref");
    }

    /**
     * A user can get ALREADY_IN_PROGRESS error on both bankID initiation tries. Logging other
     * errors as well.
     */
    private void handleKnownBankIdInitError(InitBankIdResponse initBankIdResponse)
            throws BankIdException {
        ErrorEntity error = initBankIdResponse.getError();

        if (error == null) {
            return;
        }

        if (error.isBankIdAlreadyInProgressError()) {
            throw BankIdError.ALREADY_IN_PROGRESS.exception();
        }

        if (!Strings.isNullOrEmpty(error.getCode())) {
            log.error("BankID initiation failed with error code: {}", error.getCode());
        }
    }

    public BankIdStatus collectBankId(String orderRef) {
        CollectBankIdResponse response =
                apiClient.post(
                        bankIdUrl + NordnetConstants.Urls.BANKID_COLLECT_SUFFIX,
                        new CollectBankIdRequest(orderRef),
                        CollectBankIdResponse.class);

        return response.getStatus();
    }

    public Optional<String> completeBankId(String orderRef) throws LoginException {
        String html =
                apiClient.post(
                        bankIdUrl + NordnetConstants.Urls.BANKID_COMPLETE_SUFFIX,
                        new CollectBankIdRequest(orderRef),
                        String.class);
        CompleteBankIdPage completePage = new CompleteBankIdPage(html);

        SAMLRequest request = SAMLRequest.from(completePage);
        apiClient.postForm(completePage.getTarget(), request);

        if (!apiClient.getReferrer().isPresent()) {
            return Optional.empty();
        }

        String samlArtifact = URLDecoder.decode(getSamlArtifact(apiClient.getReferrer().get()));

        // do anonymous login to populate `ntag`
        anonymousLoginForBankId();

        MultivaluedMapImpl artifactMap = new MultivaluedMapImpl();
        artifactMap.putSingle(NordnetConstants.Saml.ARTIFACT, samlArtifact);

        ArtifactResponse artifactResponse;

        try {
            artifactResponse =
                    apiClient
                            .createClientRequest(Urls.AUTHENTICATION_SAML_ARTIFACT)
                            .header(HeaderKeys.NTAG, apiClient.getNtag())
                            .type(MediaType.APPLICATION_FORM_URLENCODED)
                            .post(ArtifactResponse.class, artifactMap);

        } catch (UniformInterfaceException e) {
            ClientResponse response = e.getResponse();

            if (response != null && response.getStatus() == HttpStatus.FORBIDDEN_403) {
                throw LoginError.NOT_CUSTOMER.exception(e);
            }

            throw e;
        }

        if (!artifactResponse.isLogged_in()) {
            return Optional.empty();
        }

        URI location =
                apiClient
                        .createClientRequest(Urls.OAUTH2_AUTHORIZE_URL)
                        .get(ClientResponse.class)
                        .getLocation();

        String authCode = getAuthCodeFrom(location);
        return fetchToken(authCode);
    }

    private String getSamlArtifact(String location) {
        Matcher matcher = FIND_SAMLART_FROM_URI.matcher(location);
        return matcher.find() ? matcher.group(1) : null;
    }

    private String getAuthCodeFrom(URI location) {
        Matcher matcher = Patterns.FIND_CODE_FROM_URI.matcher(location.toASCIIString());
        return matcher.find() ? matcher.group(1) : null;
    }

    private Optional<String> fetchToken(String authCode) {
        if (Strings.isNullOrEmpty(authCode)) {
            return Optional.empty();
        }

        FetchTokenRequest tokenRequest =
                FetchTokenRequest.from(
                        QueryParamValues.CLIENT_ID, QueryParamValues.CLIENT_SECRET, authCode);
        TokenResponse response =
                apiClient.postForm(Urls.FETCH_TOKEN_URL, tokenRequest, TokenResponse.class);

        String accessToken = response.getAccessToken();
        setAccessToken(accessToken);

        return Optional.ofNullable(accessToken);
    }

    private void setAccessToken(String accessToken) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(accessToken), "No accessToken provided");
        // Store tokens in sensitive payload, so it will be masked from logs
        credentials.setSensitivePayload(Key.ACCESS_TOKEN, accessToken);
        apiClient.setAccessToken(accessToken);
    }

    private ClientResponse loadLoginPage() {
        return apiClient.get(Urls.LOGIN_PAGE_URL);
    }

    private void anonymousLoginForBankId() {
        // This request will set the NOW cookie needed for subsequent requests
        ClientResponse response =
                apiClient
                        .createClientRequest(
                                Urls.INIT_LOGIN_SESSION_URL_BANKID,
                                MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                        .post(ClientResponse.class);

        String ntag = response.getHeaders().getFirst(HeaderKeys.NTAG);
        Preconditions.checkNotNull(ntag, "Expected ntag header to exist for subsequent requests");
        apiClient.setNtag(ntag);

        LoginAnonymousPostResponse loginResponse =
                response.getEntity(LoginAnonymousPostResponse.class);

        Preconditions.checkState(loginResponse.getLoggedIn(), "Anonymous login should be true");
        Preconditions.checkState(
                loginResponse.getExpiresIn() > 0, "Expecting expiry to be larger than 0");
        Preconditions.checkState(
                Objects.equals(
                        loginResponse.getSessionType(), NordnetConstants.Session.TYPE_ANONYMOUS),
                "Expecting session type to be anonymous");
    }
}
