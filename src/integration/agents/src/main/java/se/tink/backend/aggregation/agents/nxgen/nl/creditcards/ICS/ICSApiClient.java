package se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS;

import static io.vavr.Predicates.not;

import com.google.common.base.Strings;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;
import org.apache.http.HttpHeaders;
import se.tink.backend.aggregation.agents.consent.generators.nl.ics.IcsConsentGenerator;
import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.ICSConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.ICSConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.ICSConstants.HeaderValues;
import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.ICSConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.ICSConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.ICSConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.ICSConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.authenticator.ICSOAuthGrantTypes;
import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.authenticator.ICSOAuthTokenFactory;
import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.authenticator.rpc.AccountSetupRequest;
import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.authenticator.rpc.AccountSetupResponse;
import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.authenticator.rpc.ClientCredentialTokenResponse;
import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.configuration.ICSConfiguration;
import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.fetchers.credit.rpc.CreditAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.fetchers.credit.rpc.CreditBalanceResponse;
import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.fetchers.credit.rpc.CreditTransactionsResponse;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
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
@RequiredArgsConstructor
public class ICSApiClient {

    private final TinkHttpClient client;
    private final SessionStorage sessionStorage;
    private final PersistentStorage persistentStorage;
    private final String redirectUri;
    private final ICSConfiguration configuration;
    private final String customerIpAddress;
    private final AgentComponentProvider componentProvider;
    private final ICSOAuthTokenFactory tokenFactory;
    private final ICSTimeProvider timeProvider;
    private final RandomValueGenerator randomValueGenerator;

    public ICSConfiguration getConfiguration() {
        return Optional.ofNullable(configuration)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_CONFIGURATION));
    }

    private RequestBuilder createRequest(String url) {
        return client.request(Urls.BASE + url);
    }

    private RequestBuilder createAuthRequest(String url) {
        return client.request(Urls.AUTH_BASE + url);
    }

    public RequestBuilder createAuthorizeRequest(String state, String accountRequestId) {

        return createAuthRequest(Urls.OAUTH_AUTHORIZE)
                .queryParam(QueryKeys.GRANT_TYPE, ICSOAuthGrantTypes.AUTHORIZATION_CODE.toString())
                .queryParam(QueryKeys.CLIENT_ID, getConfiguration().getClientId())
                .queryParam(QueryKeys.SCOPE, QueryValues.SCOPE_ACCOUNTS)
                .queryParam(QueryKeys.ACCOUNT_REQUEST_ID, accountRequestId)
                .queryParam(QueryKeys.STATE, state)
                .queryParam(QueryKeys.REDIRECT_URI, redirectUri)
                .queryParam(QueryKeys.RESPONSE_TYPE, QueryValues.RESPONSE_TYPE_CODE);
    }

    private RequestBuilder createRequestInSession(String url, OAuth2Token token) {
        final String clientId = getConfiguration().getClientId();
        final String clientSecret = getConfiguration().getClientSecret();
        final String xInteractionId = randomValueGenerator.getUUID().toString();

        return createRequest(url)
                .addBearerToken(token)
                .header(HeaderKeys.CLIENT_ID, clientId)
                .header(HeaderKeys.CLIENT_SECRET, clientSecret)
                .header(HeaderKeys.X_FAPI_FINANCIAL_ID, HeaderValues.FINANCIAL_ID)
                .header(HeaderKeys.X_FAPI_CUSTOMER_IP_ADDRESS, customerIpAddress)
                .header(HeaderKeys.X_FAPI_INTERACTION_ID, xInteractionId)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON);
    }

    public AccountSetupResponse setupAccount(OAuth2Token token) {
        final Date fromDate = timeProvider.getFromDate();
        final Date toDate = timeProvider.getToAndExpiredDate();
        final Date expirationDate = timeProvider.getToAndExpiredDate();

        List<String> permissions =
                new IcsConsentGenerator(componentProvider, ICSConfiguration.getIcsScopes())
                        .generate().stream().sorted().collect(Collectors.toList());

        final AccountSetupRequest request =
                new AccountSetupRequest().setup(permissions, fromDate, toDate, expirationDate);

        final String lastLoggedTime = timeProvider.getLastLoggedTime();

        return createRequestInSession(Urls.ACCOUNT_SETUP, token)
                .header(HeaderKeys.X_FAPI_CUSTOMER_LAST_LOGGED_TIME, lastLoggedTime)
                .post(AccountSetupResponse.class, request);
    }

    public ClientCredentialTokenResponse fetchTokenWithClientCredential() {
        return createRequest(Urls.OAUTH_TOKEN)
                .type(MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                .body(tokenFactory.clientCredentialsToken())
                .post(ClientCredentialTokenResponse.class);
    }

    public OAuth2Token fetchToken(String authCode) {
        sessionStorage
                .get(StorageKeys.STATE, String.class)
                .filter(not(Strings::isNullOrEmpty))
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_STATE));

        return createRequest(Urls.OAUTH_TOKEN)
                .type(MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                .body(tokenFactory.consentAuthorizationToken(authCode))
                .post(TokenResponse.class)
                .toTinkToken();
    }

    public OAuth2Token refreshToken(String refreshToken) {
        return createRequest(Urls.OAUTH_TOKEN)
                .type(MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                .body(tokenFactory.refreshToken(refreshToken))
                .post(TokenResponse.class)
                .toTinkToken();
    }

    public void setToken(OAuth2Token token) {
        persistentStorage.put(StorageKeys.TOKEN, token);
    }

    private OAuth2Token getToken() {
        return persistentStorage
                .get(StorageKeys.TOKEN, OAuth2Token.class)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_TOKEN));
    }

    public CreditAccountsResponse getAllAccounts() {
        return createRequestInSession(Urls.ACCOUNT, getToken()).get(CreditAccountsResponse.class);
    }

    public CreditBalanceResponse getAccountBalance(String accountId) {
        final String url = String.format(Urls.BALANCES, accountId);

        return createRequestInSession(url, getToken()).get(CreditBalanceResponse.class);
    }

    public CreditTransactionsResponse getTransactionsByDate(
            String accountId, LocalDate fromDate, LocalDate toDate) {
        final String url = String.format(Urls.TRANSACTIONS, accountId);
        return createRequestInSession(url, getToken())
                .queryParam(QueryKeys.FROM_BOOKING_DATE, fromDate.toString())
                .queryParam(QueryKeys.TO_BOOKING_DATE, toDate.toString())
                .get(CreditTransactionsResponse.class);
    }
}
