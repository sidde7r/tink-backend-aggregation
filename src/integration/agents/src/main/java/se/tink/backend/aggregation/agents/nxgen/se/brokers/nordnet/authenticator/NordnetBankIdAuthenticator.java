package se.tink.backend.aggregation.agents.nxgen.se.brokers.nordnet.authenticator;

import static se.tink.backend.aggregation.agents.bankid.status.BankIdStatus.DONE;
import static se.tink.backend.aggregation.agents.nxgen.se.brokers.nordnet.NordnetConstants.Patterns.FIND_BANKID_URL;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import javax.ws.rs.core.MediaType;
import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.bankid.status.BankIdStatus;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.BankIdException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.errors.BankIdError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.nordnet.NordnetApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.nordnet.NordnetConstants;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.nordnet.authenticator.html.CompleteBankIdPage;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.nordnet.authenticator.rpc.ArtifactRequest;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.nordnet.authenticator.rpc.ArtifactResponse;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.nordnet.authenticator.rpc.BankIdCollectRequest;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.nordnet.authenticator.rpc.BankIdCollectResponse;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.nordnet.authenticator.rpc.BankIdInitResponse;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.nordnet.authenticator.rpc.BankIdInitSamlResponse;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.nordnet.authenticator.rpc.InitBankIdRequest;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.nordnet.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.BankIdAuthenticator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.form.Form;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class NordnetBankIdAuthenticator implements BankIdAuthenticator<BankIdInitResponse> {

    private final NordnetApiClient apiClient;
    private final SessionStorage sessionStorage;
    private String bankIdUrl;
    private String givenSsn;

    public NordnetBankIdAuthenticator(NordnetApiClient apiClient, SessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public BankIdInitResponse init(String ssn)
            throws BankIdException, BankServiceException, AuthorizationException,
                    AuthenticationException {

        loadLoginPage();

        getSamlRequest(getBankIdInitSamlResponse());

        BankIdInitResponse bankIdInitResponse = bankIdOrder(ssn);

        sessionStorage.put(
                NordnetConstants.StorageKeys.ORDER_REF, bankIdInitResponse.getOrderRef());

        sessionStorage.put(
                NordnetConstants.StorageKeys.AUTO_START_TOKEN,
                bankIdInitResponse.getAutoStartToken());

        this.givenSsn = ssn;

        return bankIdInitResponse;
    }

    @Override
    public BankIdStatus collect(BankIdInitResponse reference)
            throws AuthenticationException, AuthorizationException {
        try {

            BankIdCollectResponse bankIdCollectResponse =
                    apiClient.post(
                            reference.getCollectUrl(),
                            BankIdCollectResponse.class,
                            reference.getOrderRef());

            BankIdStatus status = bankIdCollectResponse.getBankIdStatus();
            if (status == DONE) {

                OAuth2Token token = completeBankId();

                sessionStorage.put(NordnetConstants.StorageKeys.OAUTH_TOKEN, token);

                verifyIdentity();
            }
            return status;

        } catch (HttpResponseException e) {
            handlePollBankIdErrors(e);

            // re-throw unknown error
            throw e;
        }
    }

    private void verifyIdentity() throws LoginException {
        String ssn = apiClient.fetchIdentityData().getIdentityData().getSsn();
        if (!ssn.equalsIgnoreCase(this.givenSsn)) {
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }
    }

    @Override
    public Optional<String> getAutostartToken() {
        return Optional.of(sessionStorage.get(NordnetConstants.StorageKeys.AUTO_START_TOKEN));
    }

    @Override
    public Optional<OAuth2Token> getAccessToken() {
        return sessionStorage.get(NordnetConstants.StorageKeys.OAUTH_TOKEN, OAuth2Token.class);
    }

    @Override
    public Optional<OAuth2Token> refreshAccessToken(String refreshToken) throws SessionException {
        return Optional.empty();
    }

    private HttpResponse loadLoginPage() {
        RequestBuilder requestBuilder =
                apiClient
                        .createBasicRequest(
                                new URL(NordnetConstants.Urls.BASE_OAUTH2_AUTHORIZE)
                                        .queryParam(
                                                NordnetConstants.QueryKeys.AUTH_TYPE,
                                                NordnetConstants.QueryParamValues.SIGN_IN)
                                        .queryParam(
                                                NordnetConstants.QueryKeys.CLIENT_ID,
                                                NordnetConstants.QueryParamValues.CLIENT_ID)
                                        .queryParam(
                                                NordnetConstants.QueryKeys.RESPONSE_TYPE,
                                                NordnetConstants.QueryParamValues.RESPONSE_TYPE)
                                        .queryParam(
                                                NordnetConstants.QueryKeys.REDIRECT_URI_LOGIN,
                                                NordnetConstants.QueryParamValues
                                                        .REDIRECT_URI_LOGIN))
                        .type(MediaType.APPLICATION_JSON_TYPE);

        HttpResponse response = apiClient.get(requestBuilder, HttpResponse.class);
        apiClient.setNextReferrer(response.getHeaders());
        return response;
    }

    private BankIdInitSamlResponse getBankIdInitSamlResponse() {
        return apiClient.get(
                new URL(NordnetConstants.Urls.LOGIN_BANKID_PAGE_URL)
                        .queryParam(
                                NordnetConstants.QueryKeys.EID_METHOD,
                                NordnetConstants.QueryParamValues.EID_METHOD),
                BankIdInitSamlResponse.class);
    }

    private BankIdInitResponse bankIdOrder(String ssn) throws BankIdException {
        BankIdInitResponse response =
                apiClient.post(
                        bankIdUrl.concat(NordnetConstants.Urls.BANKID_ORDER_SUFFIX),
                        BankIdInitResponse.class,
                        new InitBankIdRequest(ssn));

        if (response.getError() != null
                && response.getError()
                        .getCode()
                        .equalsIgnoreCase(NordnetConstants.Errors.ALREADY_IN_SESSION)) {
            throw BankIdError.ALREADY_IN_PROGRESS.exception();
        }
        return response;
    }

    private HttpResponse authenticationLogin() {
        return apiClient.post(
                NordnetConstants.Urls.INIT_LOGIN_SESSION_URL_BANKID, HttpResponse.class, null);
    }

    private void getSamlRequest(BankIdInitSamlResponse bankIdInitSamlResponse) {
        HttpResponse response =
                apiClient.get(new URL(bankIdInitSamlResponse.getRequestUrl()), HttpResponse.class);
        response =
                apiClient.get(
                        new URL(
                                response.getHeaders().get(NordnetConstants.HeaderKeys.LOCATION)
                                        .stream()
                                        .findFirst()
                                        .get()),
                        HttpResponse.class);
        String url = response.getBody(String.class);
        Matcher matcher = FIND_BANKID_URL.matcher(url);
        Preconditions.checkState(matcher.find(), "Couldn't find url to initiate BankID");
        bankIdUrl = matcher.group();
    }

    private BankIdStatus handlePollBankIdErrors(HttpResponseException e)
            throws BankIdException, LoginException {
        if (e.getResponse().getStatus() == HttpStatus.SC_FORBIDDEN) {
            throw LoginError.NOT_CUSTOMER.exception();
        }
        throw BankIdError.UNKNOWN.exception();
    }

    private Optional<OAuth2Token> fetchToken(String authCode) {
        if (Strings.isNullOrEmpty(authCode)) {
            return Optional.empty();
        }

        Form formData =
                Form.builder()
                        .put(
                                NordnetConstants.FormKeys.GRANT_TYPE,
                                NordnetConstants.FormValues.AUTHORIZATION_CODE)
                        .put(
                                NordnetConstants.FormKeys.REDIRECT_URI,
                                NordnetConstants.FormValues.REDIRECT_URI)
                        .put(
                                NordnetConstants.FormKeys.CLIENT_ID,
                                NordnetConstants.QueryParamValues.CLIENT_ID)
                        .put(
                                NordnetConstants.FormKeys.CLIENT_SECRET,
                                NordnetConstants.QueryParamValues.CLIENT_SECRET)
                        .put(NordnetConstants.FormKeys.CODE, authCode)
                        .build();

        RequestBuilder requestBuilder =
                apiClient
                        .createBasicRequest(new URL(NordnetConstants.Urls.FETCH_TOKEN_URL))
                        .type(MediaType.APPLICATION_FORM_URLENCODED)
                        .body(formData.serialize());

        TokenResponse response = apiClient.post(requestBuilder, TokenResponse.class);
        return Optional.ofNullable(response.toTinkToken());
    }

    private OAuth2Token completeBankId() throws BankIdException {

        CompleteBankIdPage completeBankIdPage = getCompleteBankIdPage();

        getArtifactResponse(getSamlArtResponse(completeBankIdPage), getNtag());

        String code = getCode();

        return fetchToken(code)
                .orElseThrow(
                        () ->
                                BankIdError.UNKNOWN.exception(
                                        "Something went wrong when fetching token"));
    }

    private CompleteBankIdPage getCompleteBankIdPage() {

        String html =
                apiClient.post(
                        bankIdUrl.concat(NordnetConstants.Urls.BANKID_COMPLETE_SUFFIX),
                        String.class,
                        new BankIdCollectRequest(
                                sessionStorage.get(NordnetConstants.StorageKeys.ORDER_REF)));

        return new CompleteBankIdPage(html);
    }

    private String getSamlArtResponse(CompleteBankIdPage completeBankIdPage) {
        Form formData =
                Form.builder()
                        .put(
                                NordnetConstants.FormKeys.SAML_REQUEST,
                                completeBankIdPage.getSamlResponse())
                        .put(NordnetConstants.FormKeys.TARGET, completeBankIdPage.getTarget())
                        .build();

        RequestBuilder requestBuilder =
                apiClient
                        .createBasicRequest(new URL(completeBankIdPage.getTarget()))
                        .type(MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                        .body(formData.serialize());

        HttpResponse response = apiClient.post(requestBuilder, HttpResponse.class);

        try {
            String samlArtifact =
                    URLDecoder.decode(
                            Objects.requireNonNull(
                                    getSamlArtifact(
                                            (response.getHeaders()
                                                    .get(NordnetConstants.HeaderKeys.LOCATION)
                                                    .stream()
                                                    .findFirst()
                                                    .orElse("")))),
                            "UTF-8");
            Preconditions.checkArgument(
                    !Strings.isNullOrEmpty(samlArtifact), "Could not retrieve artifact code");
            apiClient.setReferrer(samlArtifact);
            return samlArtifact;

        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException("Could not decode artifact response");
        }
    }

    private String getNtag() {
        HttpResponse response = authenticationLogin();
        String ntag = response.getHeaders().getFirst(NordnetConstants.HeaderKeys.NTAG);
        Preconditions.checkNotNull(ntag, "Expected ntag header to exist for subsequent requests");
        return ntag;
    }

    private void getArtifactResponse(String samlArtifact, String ntag) {
        RequestBuilder request =
                apiClient
                        .createBasicRequest(
                                new URL(NordnetConstants.Urls.AUTHENTICATION_SAML_ARTIFACT))
                        .type(MediaType.APPLICATION_JSON)
                        .header(NordnetConstants.HeaderKeys.NTAG, ntag)
                        .body(new ArtifactRequest(samlArtifact));
        ArtifactResponse artifactResponseBody = apiClient.post(request, ArtifactResponse.class);

        if (!artifactResponseBody.isLoggedIn()) {
            throw BankServiceError.SESSION_TERMINATED.exception();
        }
    }

    private String getCode() {
        HttpResponse response =
                apiClient.get(
                        new URL(NordnetConstants.Urls.BASE_OAUTH2_AUTHORIZE)
                                .queryParam(
                                        NordnetConstants.QueryKeys.CLIENT_ID,
                                        NordnetConstants.QueryParamValues.CLIENT_ID)
                                .queryParam(
                                        NordnetConstants.QueryKeys.RESPONSE_TYPE,
                                        NordnetConstants.QueryParamValues.RESPONSE_TYPE)
                                .queryParam(
                                        NordnetConstants.QueryKeys.REDIRECT_URI,
                                        NordnetConstants.QueryParamValues.REDIRECT_URI),
                        HttpResponse.class);

        return getAuthCodeFrom(
                response.getHeaders().get(NordnetConstants.HeaderKeys.LOCATION).stream()
                        .findFirst()
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "Something went wrong when fetching the authCode")));
    }

    private String getSamlArtifact(String location) {
        Matcher matcher = NordnetConstants.Patterns.FIND_SAMLART_FROM_URI.matcher(location);
        return matcher.find() ? matcher.group(1) : null;
    }

    private String getAuthCodeFrom(String location) {
        Matcher matcher = NordnetConstants.Patterns.FIND_CODE_FROM_URI.matcher(location);
        return matcher.find() ? matcher.group(1) : null;
    }
}
