package se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS;

import com.google.common.base.Strings;
import java.util.Calendar;
import java.util.Date;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import javax.ws.rs.core.MediaType;
import org.apache.http.HttpHeaders;
import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.ICSConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.ICSConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.authenticator.rpc.AccountSetupRequest;
import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.authenticator.rpc.AccountSetupResponse;
import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.authenticator.rpc.ClientCredentialTokenResponse;
import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.configuration.ICSConfiguration;
import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.fetchers.credit.rpc.CreditAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.fetchers.credit.rpc.CreditBalanceResponse;
import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.fetchers.credit.rpc.CreditTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.ICSConstants.ErrorMessages;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

/*
       Flow:
            1. Get token with client_credential grant
            2. Setup account request with permissions
            3. Redirect user to ICS site to authenticate & accept permissions
            4. Fetch OAUTH2 token
            5. Fetch accounts & transactions
*/

public class ICSApiClient {

    private final TinkHttpClient client;
    private final SessionStorage sessionStorage;
    private final PersistentStorage persistentStorage;
    private final String redirectUri;
    private ICSConfiguration configuration;

    public ICSApiClient(
            TinkHttpClient client,
            SessionStorage sessionStorage,
            PersistentStorage persistentStorage,
            String redirectUri) {
        this.client = client;
        this.sessionStorage = sessionStorage;
        this.persistentStorage = persistentStorage;
        this.redirectUri = redirectUri;
    }

    public ICSConfiguration getConfiguration() {
        return Optional.ofNullable(configuration)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_CONFIGURATION));
    }

    public void setConfiguration(ICSConfiguration configuration) {
        this.configuration = configuration;
    }

    // AUTH start

    public URL getAuthorizeUrl(String state) {

        // 1. get token with client_credentials
        ClientCredentialTokenResponse clientCredentialTokenResponse =
                getTokenWithClientCredential();

        // 2. setup account & get accountRequestId
        AccountSetupResponse accountSetupResponse =
                accountSetup(clientCredentialTokenResponse.toTinkToken());

        // Verifying we get all permissions from the user
        // The documentation does not specify what permissions are required for the endpoints
        if (!receivedAllReadPermissions(accountSetupResponse)) {
            throw new IllegalStateException("Did not receive all permissions!");
        }

        this.sessionStorage.put(ICSConstants.Storage.STATE, state);

        return getAuthRequest(ICSConstants.URL.OAUTH_AUTHORIZE)
                .queryParam(QueryKeys.GRANT_TYPE, QueryValues.GRANT_TYPE_AUTH_CODE)
                .queryParam(QueryKeys.CLIENT_ID, getConfiguration().getClientId())
                .queryParam(QueryKeys.SCOPE, QueryValues.SCOPE_ACCOUNTS)
                .queryParam(
                        QueryKeys.ACCOUNT_REQUEST_ID,
                        accountSetupResponse.getData().getAccountRequestId())
                .queryParam(QueryKeys.STATE, state)
                .queryParam(QueryKeys.REDIRECT_URI, redirectUri)
                .queryParam(QueryKeys.RESPONSE_TYPE, QueryValues.RESPONSE_TYPE_CODE)
                .getUrl();
    }

    public AccountSetupResponse accountSetup(OAuth2Token token) {
        Date fromDate = getFromDate();
        Date toDate = getToDate();
        Date expirationDate = getExpirationDate();

        AccountSetupRequest request =
                new AccountSetupRequest()
                        .setup(
                                ICSConstants.Permissions.ALL_READ_PERMISSIONS,
                                fromDate,
                                toDate,
                                expirationDate);

        String lastLoggedTime = getLastLoggedTime(new Date());

        return createRequestInSession(ICSConstants.URL.ACCOUNT_SETUP, token)
                .header(ICSConstants.Headers.X_JWS_SIGNATURE, getJWSSignature(request))
                .header(ICSConstants.Headers.X_FAPI_CUSTOMER_LAST_LOGGED_TIME, lastLoggedTime)
                .post(AccountSetupResponse.class, request);
    }

    private ClientCredentialTokenResponse getTokenWithClientCredential() {
        return createRequest(ICSConstants.URL.OAUTH_TOKEN)
                .queryParam(QueryKeys.CLIENT_ID, getConfiguration().getClientId())
                .queryParam(QueryKeys.CLIENT_SECRET, getConfiguration().getClientSecret())
                .queryParam(
                        QueryKeys.GRANT_TYPE,
                        QueryValues.GRANT_TYPE_CLIENT_CREDENTIALS)
                .queryParam(QueryKeys.SCOPE, QueryValues.SCOPE_ACCOUNTS)
                .get(ClientCredentialTokenResponse.class);
    }

    private RequestBuilder getAuthRequest(String resource) {
        return client.request(ICSConstants.URL.AUTH_BASE + resource);
    }

    private boolean receivedAllReadPermissions(AccountSetupResponse response) {
        return response.getData()
                .getPermissions()
                .equals(ICSConstants.Permissions.ALL_READ_PERMISSIONS);
    }

    private String getInteractionId() {
        return UUID.randomUUID().toString();
    }

    // Not required to send actual values, sending dummy data
    private String getFinancialId() {
        return "e3213dfd-435fgrd5-e7edr4";
    }

    // Not required to send actual values, sending dummy data
    private String getCustomerIpAdress() {
        return "234.213.323.123";
    }

    // Not required to send actual values, sending dummy data
    private String getJWSSignature(AccountSetupRequest request) {
        return "";
    }

    // Can fetch transactions max 2 years back
    private Date getFromDate() {
        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        c.add(Calendar.YEAR, -2);
        return c.getTime();
    }

    private Date getToDate() {
        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        return c.getTime();
    }

    // Can be max 90 days in the future
    private Date getExpirationDate() {
        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        c.add(Calendar.DATE, 90);
        return c.getTime();
    }

    private String getLastLoggedTime(Date date) {
        return ICSConstants.Date.LAST_LOGGED_TIME_FORMAT.format(date);
    }

    // AUTH end

    // API start
    public void setToken(OAuth2Token token) {
        this.persistentStorage.put(ICSConstants.Storage.TOKEN, token);
    }

    private OAuth2Token getToken() {
        return this.persistentStorage
                .get(ICSConstants.Storage.TOKEN, OAuth2Token.class)
                .orElseThrow(() -> new NoSuchElementException("Token missing"));
    }

    public OAuth2Token fetchToken(String authCode) {
        String state = this.sessionStorage.get(ICSConstants.Storage.STATE);

        if (Strings.isNullOrEmpty(state)) {
            throw new IllegalStateException("state cannot be null or empty!");
        }

        return createRequest(ICSConstants.URL.OAUTH_TOKEN)
                .queryParam(QueryKeys.GRANT_TYPE, QueryValues.GRANT_TYPE_AUTH_CODE)
                .queryParam(QueryKeys.CLIENT_ID, getConfiguration().getClientId())
                .queryParam(QueryKeys.CLIENT_SECRET, getConfiguration().getClientSecret())
                .queryParam(QueryKeys.REDIRECT_URI, redirectUri)
                .queryParam(QueryKeys.AUTH_CODE, authCode)
                .queryParam(QueryKeys.STATE, state)
                .get(TokenResponse.class)
                .toTinkToken();
    }

    public OAuth2Token refreshToken(String refreshToken) {
        return createRequest(ICSConstants.URL.OAUTH_TOKEN)
                .queryParam(
                        QueryKeys.GRANT_TYPE, QueryValues.GRANT_TYPE_REFRESH_TOKEN)
                .queryParam(QueryKeys.CLIENT_ID, getConfiguration().getClientId())
                .queryParam(QueryKeys.CLIENT_SECRET, getConfiguration().getClientSecret())
                .queryParam(QueryKeys.REFRESH_TOKEN, refreshToken)
                .get(OAuth2Token.class);
    }

    private RequestBuilder createRequest(String url) {
        return client.request(ICSConstants.URL.BASE + url);
    }

    private RequestBuilder createRequestInSession(String url, OAuth2Token token) {
        final String clientId = getConfiguration().getClientId();
        final String clientSecret = getConfiguration().getClientSecret();
        final String xFinancialID = getFinancialId();
        final String xCustomerIPAddress = getCustomerIpAdress();
        final String xInteractionId = getInteractionId();

        return createRequest(url)
                .addBearerToken(token)
                .header(ICSConstants.Headers.CLIENT_ID, clientId)
                .header(ICSConstants.Headers.CLIENT_SECRET, clientSecret)
                .header(ICSConstants.Headers.X_FAPI_FINANCIAL_ID, xFinancialID)
                .header(ICSConstants.Headers.X_FAPI_CUSTOMER_IP_ADDRESS, xCustomerIPAddress)
                .header(ICSConstants.Headers.X_FAPI_INTERACTION_ID, xInteractionId)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON);
    }

    // API end

    // AIS
    public CreditAccountsResponse getAllAccounts() {
        return createRequestInSession(ICSConstants.URL.ACCOUNT, getToken())
                .get(CreditAccountsResponse.class);
    }

    public CreditBalanceResponse getAccountBalance(String accountId) {
        final String url = String.format(ICSConstants.URL.BALANCES, accountId);

        return createRequestInSession(url, getToken()).get(CreditBalanceResponse.class);
    }

    public CreditTransactionsResponse getTransactions(String accountId) {
        final String url = String.format(ICSConstants.URL.TRANSACTIONS, accountId);

        return createRequestInSession(url, getToken()).get(CreditTransactionsResponse.class);
    }
}
