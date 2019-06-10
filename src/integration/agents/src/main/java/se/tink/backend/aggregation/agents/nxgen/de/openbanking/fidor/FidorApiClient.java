package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fidor;

import java.util.Optional;
import java.util.UUID;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fidor.FidorConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fidor.FidorConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fidor.FidorConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fidor.FidorConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fidor.FidorConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fidor.FidorConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fidor.authenticator.rpc.AuthorizationConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fidor.authenticator.rpc.AutorizationConsentRequest;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fidor.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fidor.authenticator.rpc.CreateConsentRequest;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fidor.authenticator.rpc.TokenRequest;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fidor.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fidor.configuration.FidorConfiguration;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fidor.entities.CreateAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fidor.entities.FlagsEntity;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fidor.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fidor.fetcher.transactionalaccount.rpc.AccountFetchResponse;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fidor.fetcher.transactionalaccount.rpc.BalanceFetchResponse;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fidor.fetcher.transactionalaccount.rpc.TransactionFetchResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public final class FidorApiClient {

    private final TinkHttpClient client;
    private final PersistentStorage persistentStorage;
    private FidorConfiguration configuration;

    public FidorApiClient(TinkHttpClient client, PersistentStorage persistentStorage) {
        this.client = client;
        this.persistentStorage = persistentStorage;
    }

    private FidorConfiguration getConfiguration() {
        return Optional.ofNullable(configuration)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_CONFIGURATION));
    }

    protected void setConfiguration(FidorConfiguration configuration) {
        this.configuration = configuration;
    }

    public TokenResponse OAut2_Password(String username, String password) {
        String clientId = getConfiguration().getClientId();
        String clientSecret = getConfiguration().getClientSecret();

        TokenRequest request = new TokenRequest(QueryValues.GRANT_TYPE, username, password);

        return client.request(Urls.OAUTH_PASSWORD)
                .type(MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_JSON)
                .addBasicAuth(clientId, clientSecret)
                .post(TokenResponse.class, request.toOautPasswordData());
    }

    public AuthorizationConsentResponse authorizeConsent(
            String scaAuthenticationData, String authroizationLink) {
        final String baseUrl = getConfiguration().getBaseUrl();
        AutorizationConsentRequest requestBody =
                new AutorizationConsentRequest(scaAuthenticationData);

        return createRequestInSession(new URL(baseUrl + authroizationLink))
                .put(AuthorizationConsentResponse.class, requestBody);
    }

    public ConsentResponse getConsent(String iban, String bban) {

        CreateConsentRequest request = new CreateConsentRequest(iban, bban, "9999-12-31", true, 4);

        return createRequestInSession(new URL(Urls.CONSENTS)).post(ConsentResponse.class, request);
    }

    public AccountFetchResponse fetchAccouns() {
        AccountFetchResponse accountsResponse =
                createRequestWithConsent(new URL(Urls.FETCH_ACCOUNTS))
                        .get(AccountFetchResponse.class);

        for (AccountEntity entity : accountsResponse.getAccounts()) {
            BalanceFetchResponse balanceResponse = fetchBalances(entity.getResourceId());
            entity.setBalances(balanceResponse.getBalances());
        }

        return accountsResponse;
    }

    public TransactionFetchResponse fetchTransactions(TransactionalAccount account, int page) {
        return createRequestWithConsent(
                        new URL(
                                String.format(
                                        Urls.FETCH_TRANSACTIONS,
                                        account.getFromTemporaryStorage(StorageKeys.ACCOUNT_ID))))
                .queryParam(QueryKeys.PAGE, Integer.toString(page))
                .get(TransactionFetchResponse.class);
    }

    public BalanceFetchResponse fetchBalances(String accountId) {
        return createRequestWithConsent(new URL(String.format(Urls.FETCH_BALANCES, accountId)))
                .get(BalanceFetchResponse.class);
    }

    // not used currently, but i had to create account this way so i could use their  APIs
    //// thought i'd leave this if someone else would need it
    public String createTestAccount(String accessToken) {

        final String baseUrl = getConfiguration().getBaseUrl();
        CreateAccountEntity entity =
                new CreateAccountEntity(
                        "s.jankovic@vegaitsourcing.rs",
                        "password123",
                        new FlagsEntity(true, true, true));
        return client.request(baseUrl + Urls.CREATE_ACCOUNT)
                .type(MediaType.APPLICATION_JSON)
                .header(HeaderKeys.X_REQUEST_ID, UUID.randomUUID().toString())
                .header("Authorization", "Bearer " + accessToken)
                .post(String.class, entity);
    }

    // not used currently but i had to onboard first so i could use their APIs
    // thought i'd leave this if someone else would need it
    public String onboard() {
        final String baseUrl = getConfiguration().getBaseUrl();
        return client.request(baseUrl + Urls.ONBOARDING)
                .header(HeaderKeys.X_REQUEST_ID, UUID.randomUUID().toString())
                .get(String.class);
    }

    private RequestBuilder createRequest(URL url) {
        return client.request(url).type(MediaType.APPLICATION_JSON);
    }

    private RequestBuilder createRequestInSession(URL url) {
        final OAuth2Token authToken = getTokenFromStorage();

        return createRequest(url)
                .type(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .header(HeaderKeys.X_REQUEST_ID, UUID.randomUUID().toString())
                .header(HeaderKeys.PSU_IP_ADDRESS, getPsuIpAddress())
                .addBearerToken(authToken);
    }

    private RequestBuilder createRequestWithConsent(URL url) {
        final String consentId = getConsentIdFromStorage();

        return createRequestInSession(url).header(HeaderKeys.CONSENT_ID, consentId);
    }

    private OAuth2Token getTokenFromStorage() {
        return persistentStorage
                .get(StorageKeys.OAUTH_TOKEN, OAuth2Token.class)
                .orElseThrow(
                        () -> new IllegalStateException(SessionError.SESSION_EXPIRED.exception()));
    }

    public String getConsentIdFromStorage() {
        return persistentStorage
                .get(StorageKeys.CONSENT_ID, String.class)
                .orElseThrow(
                        () -> new IllegalStateException(SessionError.SESSION_EXPIRED.exception()));
    }

    public String getPsuIpAddress() {
        return "82.117.210.2";
    }
}
