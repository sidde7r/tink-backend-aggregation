package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fidor;

import java.util.Optional;
import java.util.UUID;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fidor.FidorConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fidor.FidorConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fidor.FidorConstants.HeaderValues;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fidor.FidorConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fidor.FidorConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fidor.FidorConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fidor.FidorConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fidor.authenticator.rpc.AuthorizationConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fidor.authenticator.rpc.AutorizationConsentRequest;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fidor.authenticator.rpc.ConsentRedirectResponse;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fidor.authenticator.rpc.CreateConsentRequest;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fidor.authenticator.rpc.TokenRequest;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fidor.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fidor.configuration.FidorConfiguration;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fidor.fetcher.transactionalaccount.rpc.AccountFetchResponse;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fidor.fetcher.transactionalaccount.rpc.BalanceFetchResponse;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fidor.fetcher.transactionalaccount.rpc.TransactionFetchResponse;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.url.URL;
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

    protected void setConfiguration(AgentConfiguration<FidorConfiguration> configuration) {
        this.configuration = configuration.getProviderSpecificConfiguration();
    }

    public TokenResponse getToken(String username, String password) {
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
            String scaAuthenticationData, String authorizationLink) {
        AutorizationConsentRequest requestBody =
                new AutorizationConsentRequest(scaAuthenticationData);

        return createRequestInSession(new URL(Urls.BASE_URL + authorizationLink))
                .put(AuthorizationConsentResponse.class, requestBody);
    }

    public ConsentRedirectResponse getConsent(String iban, String bban) {
        // iban and bban are passed through test, as they represent iban and bban of individual user
        // the third param represents how long the consent is valid, the inserted value means as
        // much as possible which is 90 days
        // the last param states number of requests per day, maximum is 4
        CreateConsentRequest request = new CreateConsentRequest(iban, bban, "9999-12-31", true, 4);
        return createRequestInSession(new URL(Urls.CONSENTS))
                .post(ConsentRedirectResponse.class, request);
    }

    public AccountFetchResponse fetchAccouns() {
        return createRequestWithConsent(new URL(Urls.FETCH_ACCOUNTS))
                .get(AccountFetchResponse.class);
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
                .header(HeaderKeys.TPP_REDIRECT_URI, new URL(configuration.getRedirectUri()))
                .header(HeaderKeys.TPP_REDIRECT_PREFERRED, HeaderValues.TPP_REDIRECT_PREFERRED)
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

    private String getConsentIdFromStorage() {
        return persistentStorage
                .get(StorageKeys.CONSENT_ID, String.class)
                .orElseThrow(
                        () -> new IllegalStateException(SessionError.SESSION_EXPIRED.exception()));
    }

    private String getPsuIpAddress() {
        // This is supposed to be the IP address of the PSU, but we can't supply it on sandbox so we
        // use dummy value
        return "82.117.210.2";
    }
}
