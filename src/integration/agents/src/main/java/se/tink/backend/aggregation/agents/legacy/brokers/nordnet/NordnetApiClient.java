package se.tink.backend.aggregation.agents.brokers.nordnet;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import java.net.URI;
import java.net.URLDecoder;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriBuilder;
import org.apache.http.Header;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.RedirectStrategy;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.protocol.HttpContext;
import org.eclipse.jetty.http.HttpStatus;
import se.tink.backend.aggregation.agents.BankIdStatus;
import se.tink.backend.aggregation.agents.brokers.nordnet.model.AccountEntity;
import se.tink.backend.aggregation.agents.brokers.nordnet.model.AccountInfoEntity;
import se.tink.backend.aggregation.agents.brokers.nordnet.model.AuthenticateBasicLoginRequest;
import se.tink.backend.aggregation.agents.brokers.nordnet.model.AuthenticateBasicLoginResponse;
import se.tink.backend.aggregation.agents.brokers.nordnet.model.CustomerInfoResponse;
import se.tink.backend.aggregation.agents.brokers.nordnet.model.ErrorEntity;
import se.tink.backend.aggregation.agents.brokers.nordnet.model.LoginAnonymousPostResponse;
import se.tink.backend.aggregation.agents.brokers.nordnet.model.PositionsResponse;
import se.tink.backend.aggregation.agents.brokers.nordnet.model.Request.CollectBankIdRequest;
import se.tink.backend.aggregation.agents.brokers.nordnet.model.Request.FetchTokenRequest;
import se.tink.backend.aggregation.agents.brokers.nordnet.model.Request.InitBankIdRequest;
import se.tink.backend.aggregation.agents.brokers.nordnet.model.Request.SAMLRequest;
import se.tink.backend.aggregation.agents.brokers.nordnet.model.Response.AccountInfoResponse;
import se.tink.backend.aggregation.agents.brokers.nordnet.model.Response.AccountResponse;
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
import se.tink.backend.aggregation.agents.utils.log.LogTag;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.libraries.identitydata.IdentityData;
import se.tink.libraries.net.TinkApacheHttpClient4;

public class NordnetApiClient {
    private static final Pattern FIND_CODE_FROM_URI = Pattern.compile("\\?code=([a-zA-Z\\d]*)$");
    private static final Pattern FIND_SAMLART_FROM_URI = Pattern.compile("SAMLart=([^&]*)");
    private static final Pattern FIND_BANKID_URL =
            Pattern.compile("https://nneid\\.nordnet\\.se/std/method/nordnet\\.se/[a-zA-Z\\d]*/");

    private static final String BASE_URL = "https://www.nordnet.se";

    private static final String AUTHENTICATION_BASIC_LOGIN_URL =
            BASE_URL + "/api/2/authentication/basic/login";
    private static final String AUTHENTICATION_SAML_ARTIFACT =
            BASE_URL + "/api/2/authentication/eid/saml/artifact";
    private static final String OAUTH2_AUTHORIZE_URL =
            BASE_URL
                    + "/oauth2/authorize?client_id=MOBILE_IOS&response_type=code&redirect_uri=https://www.nordnet.se/now/mobile/token.html";
    private static final String INIT_LOGIN_SESSION_URL = BASE_URL + "/api/2/login/anonymous";
    private static final String LOGIN_PAGE_URL =
            BASE_URL
                    + "/oauth2/authorize?authType=&client_id=MOBILE_IOS&response_type=code&redirect_uri=https://www.nordnet.se/now/mobile/token.html";
    private static final String LOGIN_BANKID_PAGE_URL =
            BASE_URL + "/api/2/authentication/eid/saml/request?eid_method=sbidAnother";
    private static final String FETCH_TOKEN_URL = BASE_URL + "/oauth2/token";
    private static final String GET_ACCOUNTS_SUMMARY_URL = BASE_URL + "/api/2/accounts/summary";
    private static final String GET_ACCOUNTS_URL = BASE_URL + "/api/2/accounts";
    private static final String GET_ACCOUNTS_INFO_URL = BASE_URL + "/api/2/accounts/%s/info";
    private static final String GET_POSITIONS_URL = BASE_URL + "/api/2/accounts/%s/positions";
    private static final String GET_CUSTOMER_INFO_URL = BASE_URL + "/api/2/customers/contact_info";

    private static final AggregationLogger log = new AggregationLogger(NordnetApiClient.class);
    private static final LogTag LOG_ACCOUNT_INFO = LogTag.from("Nordnet-account-info");

    private String bankIdUrl;

    private TinkApacheHttpClient4 client;
    private static final String CLIENT_SECRET = "00b1fcce-e433-48ca-9b9a-7718178852c8";

    private String referrer;
    private String accessToken;
    private String ntag;
    private final String aggregator;
    /** A concatenated string of account's bank-id (seems to be a simple client specific index) */
    private String accountBankIds;

    public NordnetApiClient(TinkApacheHttpClient4 client, String aggregator) {
        this.aggregator = aggregator;
        this.client = client;
    }

    private Optional<String> getReferrer() {
        return Optional.ofNullable(Strings.emptyToNull(referrer));
    }

    private void setNextReferrer(MultivaluedMap<String, String> headers) {
        String nextReferrer = headers.getFirst("NextReferrer");

        if (!Strings.isNullOrEmpty(nextReferrer)) {
            referrer = BASE_URL + nextReferrer;
        }
    }

    private void setAccessToken(String accessToken) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(accessToken), "No accessToken provided");

        this.accessToken = accessToken;
    }

    private ClientResponse loadLoginPage() {
        return get(LOGIN_PAGE_URL);
    }

    public Optional<String> loginWithPassword(String username, String password)
            throws LoginException {
        authorizeSession();
        initLoginSession();
        authenticate(username, password);
        String authCode = authorizeUser();
        return fetchToken(authCode);
    }

    private void authorizeSession() {
        // This request will set ENDPOINT_URL cookie
        ClientResponse response = createClientRequest(LOGIN_PAGE_URL).get(ClientResponse.class);

        URI redirectLocation = response.getLocation();
        Preconditions.checkNotNull(
                redirectLocation, "Expected redirect to /mux/login/startSE.html");

        // This request will set NOW, LOL and TUX-COOKIE
        createClientRequest(BASE_URL + redirectLocation.toASCIIString()).get(ClientResponse.class);
    }

    private void initLoginSession() {
        // This request will set the NOW cookie needed for subsequent requests
        ClientResponse response =
                createClientRequest(
                                INIT_LOGIN_SESSION_URL, MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                        .post(ClientResponse.class);

        String ntag = response.getHeaders().getFirst("ntag");
        Preconditions.checkNotNull(ntag, "Expected ntag header to exist for subsequent requests");

        this.ntag = ntag;

        LoginAnonymousPostResponse loginResponse =
                response.getEntity(LoginAnonymousPostResponse.class);

        Preconditions.checkState(loginResponse.getLoggedIn(), "Anonymous login should be true");
        Preconditions.checkState(
                loginResponse.getExpiresIn() > 0, "Expecting expiry to be larger than 0");
        Preconditions.checkState(
                Objects.equals(loginResponse.getSessionType(), "anonymous"),
                "Expecting session type to be anonymous");
    }

    private void authenticate(String username, String password) throws LoginException {
        AuthenticateBasicLoginRequest loginRequest =
                new AuthenticateBasicLoginRequest(username, password);

        ClientResponse response =
                createClientRequest(
                                AUTHENTICATION_BASIC_LOGIN_URL,
                                MediaType.APPLICATION_JSON_TYPE,
                                ImmutableMap.of("ntag", ntag))
                        .post(ClientResponse.class, loginRequest);

        AuthenticateBasicLoginResponse loginResponse =
                response.getEntity(AuthenticateBasicLoginResponse.class);

        if (Objects.equals(loginResponse.getCode(), "NEXT_LOGIN_INVALID_LOGIN_PARAMETER")) {
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }

        manage(response);

        Preconditions.checkState(loginResponse.getLoggedIn(), "Expected user to be logged in");
        Preconditions.checkState(
                Objects.equals(loginResponse.getSessionType(), "authenticated"),
                "Expected session to be of type authenticated");

        String ntag = response.getHeaders().getFirst("ntag");
        Preconditions.checkNotNull(ntag, "Expected ntag header to exist for subsequent requests");

        this.ntag = ntag;
    }

    private String authorizeUser() {
        ClientResponse response = get(LOGIN_PAGE_URL);
        URI location = response.getLocation();

        // The redirect location holds an auth code needed for requesting a token
        Preconditions.checkNotNull(location);
        Matcher matcher = FIND_CODE_FROM_URI.matcher(location.toASCIIString());
        Preconditions.checkState(matcher.find(), "Expected auth code to be present");

        return matcher.group(1);
    }

    public String initBankID(String username) throws BankIdException {
        loadLoginPage();
        BankIdInitSamlResponse bankIdInitSamlResponse =
                get(LOGIN_BANKID_PAGE_URL, BankIdInitSamlResponse.class);

        String html = get(bankIdInitSamlResponse.getRequestUrl(), String.class);
        Matcher matcher = FIND_BANKID_URL.matcher(html);

        Preconditions.checkState(matcher.find(), "Couldn't find url to initiate BankID");
        bankIdUrl = matcher.group();

        // Try to initiate the bankid auth twice (if the first one fails). It will fail the first
        // time if the customer
        // has an already active authentication (both authentications will be cancelled by bankid)
        InitBankIdResponse initBankIdResponse = null;

        for (int i = 0; i < 2; i++) {
            initBankIdResponse =
                    post(
                            bankIdUrl + "order",
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
            log.error(
                    String.format("BankID initiation failed with error code: %s", error.getCode()));
        }
    }

    public BankIdStatus collectBankId(String orderRef) {
        CollectBankIdResponse response =
                post(
                        bankIdUrl + "collect",
                        new CollectBankIdRequest(orderRef),
                        CollectBankIdResponse.class);

        return response.getStatus();
    }

    public Optional<String> completeBankId(String orderRef) throws LoginException {
        String html =
                post(bankIdUrl + "complete", new CollectBankIdRequest(orderRef), String.class);
        CompleteBankIdPage completePage = new CompleteBankIdPage(html);

        SAMLRequest request = SAMLRequest.from(completePage);
        postForm(completePage.getTarget(), request);

        if (!getReferrer().isPresent()) {
            return Optional.empty();
        }

        String samlArtifact = URLDecoder.decode(getSamlArtifact(getReferrer().get()));

        // do anonymous login to populate `ntag`
        initLoginSession();

        MultivaluedMapImpl artifactMap = new MultivaluedMapImpl();
        artifactMap.putSingle("artifact", samlArtifact);

        ArtifactResponse artifactResponse;

        try {
            artifactResponse =
                    createClientRequest(AUTHENTICATION_SAML_ARTIFACT)
                            .header("ntag", ntag)
                            .type(MediaType.APPLICATION_FORM_URLENCODED)
                            .post(ArtifactResponse.class, artifactMap);

        } catch (UniformInterfaceException e) {
            ClientResponse response = e.getResponse();

            if (response != null && response.getStatus() == HttpStatus.FORBIDDEN_403) {
                throw LoginError.NOT_CUSTOMER.exception();
            }

            throw e;
        }

        if (!artifactResponse.isLogged_in()) {
            return Optional.empty();
        }

        URI location =
                createClientRequest(OAUTH2_AUTHORIZE_URL).get(ClientResponse.class).getLocation();

        String authCode = getAuthCodeFrom(location);
        return fetchToken(authCode);
    }

    private String getSamlArtifact(String location) {
        Matcher matcher = FIND_SAMLART_FROM_URI.matcher(location);
        return matcher.find() ? matcher.group(1) : null;
    }

    private String getAuthCodeFrom(URI location) {
        Matcher matcher = FIND_CODE_FROM_URI.matcher(location.toASCIIString());
        return matcher.find() ? matcher.group(1) : null;
    }

    private Optional<String> fetchToken(String authCode) {
        if (Strings.isNullOrEmpty(authCode)) {
            return Optional.empty();
        }

        FetchTokenRequest tokenRequest = FetchTokenRequest.from(CLIENT_SECRET, authCode);
        TokenResponse response = postForm(FETCH_TOKEN_URL, tokenRequest, TokenResponse.class);

        String accessToken = response.getAccessToken();
        setAccessToken(accessToken);

        return Optional.ofNullable(accessToken);
    }

    public AccountResponse fetchAccounts() {

        String uri = UriBuilder.fromUri(GET_ACCOUNTS_URL).build().toASCIIString();
        AccountResponse accounts = this.get(uri, AccountResponse.class);

        accountBankIds =
                accounts.stream().map(a -> a.getAccountId()).collect(Collectors.joining(","));
        AccountInfoResponse infos =
                this.get(
                        String.format(GET_ACCOUNTS_INFO_URL, accountBankIds),
                        AccountInfoResponse.class);

        for (AccountEntity accountEntity : accounts) {
            String accId = accountEntity.getAccountId();

            for (AccountInfoEntity infoEntity : infos) {
                String infoId = infoEntity.getAccountId();

                if (accId.equalsIgnoreCase(infoId)) {
                    log.info(LOG_ACCOUNT_INFO + ": " + infoEntity.toString());
                    accountEntity.setInfo(infoEntity);
                    break;
                }
            }
        }

        return accounts;
    }

    public IdentityData fetchIdentityData() {
        CustomerInfoResponse customerInfo = get(GET_CUSTOMER_INFO_URL, CustomerInfoResponse.class);

        return customerInfo.toTinkIdentity();
    }

    private <T> T post(String url, Object request, Class<T> responseEntity) {
        return createClientRequest(url).post(responseEntity, request);
    }

    private <T> T postForm(String url, MultivaluedMapImpl request, Class<T> responseEntity) {
        ClientResponse response = postForm(url, request);

        return response.getEntity(responseEntity);
    }

    private ClientResponse postForm(String url, MultivaluedMap request) {
        ClientResponse response =
                createClientRequest(url)
                        .type(MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                        .post(ClientResponse.class, request);

        manage(response);
        return response;
    }

    private <T> T get(String url, Class<T> responseEntity) {
        return get(url).getEntity(responseEntity);
    }

    private ClientResponse get(String url) {
        ClientResponse response = createClientRequest(url).get(ClientResponse.class);
        manage(response);

        return response;
    }

    private void manage(ClientResponse response) {
        if (response.getStatus() >= 400) {
            throw new UniformInterfaceException(response);
        }

        setNextReferrer(response.getHeaders());
    }

    private WebResource.Builder createClientRequest(String url) {
        return createClientRequest(url, MediaType.APPLICATION_JSON_TYPE, Collections.emptyMap());
    }

    private WebResource.Builder createClientRequest(String url, MediaType contentType) {
        return createClientRequest(url, contentType, Collections.emptyMap());
    }

    private WebResource.Builder createClientRequest(
            String url, MediaType contentType, Map<String, String> headers) {
        WebResource.Builder requestBuilder =
                client.resource(url)
                        .header("User-Agent", aggregator)
                        .accept(
                                MediaType.APPLICATION_JSON_TYPE, MediaType.TEXT_HTML_TYPE,
                                MediaType.TEXT_PLAIN_TYPE, MediaType.WILDCARD_TYPE)
                        .type(contentType);

        Optional<String> referrer = getReferrer();

        for (String header : headers.keySet()) {
            requestBuilder.header(header, headers.get(header));
        }

        referrer.ifPresent(s -> requestBuilder.header("Referer", s));

        if (!Strings.isNullOrEmpty(accessToken)) {
            requestBuilder.header("Authorization", String.format("Bearer %s", accessToken));
        }

        return requestBuilder;
    }

    public static final RedirectStrategy REDIRECT_STRATEGY =
            new DefaultRedirectStrategy() {
                @Override
                public boolean isRedirected(
                        HttpRequest request, HttpResponse response, HttpContext context) {
                    String referrer = request.getRequestLine().getUri();
                    response.setHeader("NextReferrer", referrer);

                    String location = getLocationUri(response);
                    return location != null
                            && !location.startsWith("/now/mobile/")
                            && !location.startsWith("/mux/login/startse.html");
                }

                private String getLocationUri(HttpResponse response) {
                    Header header = response.getFirstHeader("Location");

                    if (header == null) {
                        return null;
                    }

                    return header.getValue().toLowerCase().replace("https://www.nordnet.se", "");
                }
            };

    public Optional<PositionsResponse> getPositions() {
        // Always fetches positions for all accounts/portfolios, but called once for each.
        try {
            ClientResponse clientResponse = get(String.format(GET_POSITIONS_URL, accountBankIds));
            PositionsResponse response = clientResponse.getEntity(PositionsResponse.class);
            return Optional.of(response);
        } catch (UniformInterfaceException e) {
            ClientResponse response = e.getResponse();
            if (response.getStatus() != 204) {
                log.warn("nordnet - position fetching failed", e);
            }
            return Optional.empty();
        }
    }
}
