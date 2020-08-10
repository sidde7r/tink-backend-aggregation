package se.tink.backend.aggregation.agents.nxgen.de.openbanking.santander;

import java.util.Optional;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.santander.SantanderConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.santander.SantanderConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.santander.SantanderConstants.HeaderValues;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.santander.SantanderConstants.IdTag;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.santander.SantanderConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.santander.SantanderConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.santander.SantanderConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.santander.SantanderConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.santander.authenticator.rpc.ConsentRequest;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.santander.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.santander.authenticator.rpc.TokenRequest;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.santander.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.santander.configuration.SantanderConfiguration;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.santander.executor.payment.rpc.CreatePaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.santander.executor.payment.rpc.CreatePaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.santander.executor.payment.rpc.FetchPaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.santander.fetcher.transactionalaccount.rpc.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.santander.fetcher.transactionalaccount.rpc.TransactionsKeyPaginatorResponse;
import se.tink.backend.aggregation.api.Psd2Headers;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public final class SantanderApiClient {

    private final TinkHttpClient client;
    private final PersistentStorage persistentStorage;
    private SantanderConfiguration configuration;

    public SantanderApiClient(TinkHttpClient client, PersistentStorage persistentStorage) {
        this.client = client;
        this.persistentStorage = persistentStorage;
    }

    private SantanderConfiguration getConfiguration() {
        return Optional.ofNullable(configuration)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_CONFIGURATION));
    }

    protected void setConfiguration(AgentConfiguration<SantanderConfiguration> configuration) {
        this.configuration = configuration.getProviderSpecificConfiguration();
    }

    public OAuth2Token getToken() {
        TokenRequest request = new TokenRequest(QueryValues.GRANT_TYPE);
        return client.request(Urls.TOKEN)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED)
                .addBasicAuth(
                        getConfiguration().getClientId(), getConfiguration().getClientSecret())
                .accept(MediaType.APPLICATION_JSON)
                .post(TokenResponse.class, request.toData())
                .toTinkToken();
    }

    public String getConsentId(String iban) {
        final ConsentRequest consentsRequest = new ConsentRequest();
        consentsRequest
                .getAccess()
                .addAccessEntity(iban, SantanderConstants.CredentialValues.CURRENCY);

        return client.request(Urls.CONSENT)
                .addBearerToken(getTokenFromStorage())
                .header(HeaderKeys.X_IBM_CLIENT_ID, getConfiguration().getClientId())
                .header(HeaderKeys.X_REQUEST_ID, Psd2Headers.getRequestId())
                .body(consentsRequest.toData(), MediaType.APPLICATION_JSON_TYPE)
                .post(ConsentResponse.class)
                .getConsentId();
    }

    public AccountsResponse fetchAccounts() {
        final String clientId = getConfiguration().getClientId();
        final String consentId = persistentStorage.get(StorageKeys.CONSENT_ID);

        return client.request(Urls.ACCOUNTS)
                .queryParam(QueryKeys.WITH_BALANCE, QueryValues.TRUE)
                .addBearerToken(getTokenFromStorage())
                .header(HeaderKeys.CONSENT_ID, consentId)
                .header(HeaderKeys.X_IBM_CLIENT_ID, clientId)
                .header(HeaderKeys.X_REQUEST_ID, Psd2Headers.getRequestId())
                .type(MediaType.APPLICATION_JSON)
                .get(AccountsResponse.class);
    }

    public TransactionsKeyPaginatorResponse fetchTransactions(String url) {
        final String consentId = persistentStorage.get(StorageKeys.CONSENT_ID);
        final String clientId = getConfiguration().getClientId();

        return client.request(Urls.TRANSACTIONS + url)
                .addBearerToken(getTokenFromStorage())
                .queryParam(QueryKeys.BOOKING_STATUS, QueryValues.BOTH)
                .header(HeaderKeys.CONSENT_ID, consentId)
                .header(HeaderKeys.X_IBM_CLIENT_ID, clientId)
                .header(HeaderKeys.X_REQUEST_ID, Psd2Headers.getRequestId())
                .get(TransactionsKeyPaginatorResponse.class);
    }

    public void setTokenToStorage(OAuth2Token token) {
        persistentStorage.put(StorageKeys.OAUTH_TOKEN, token);
    }

    private OAuth2Token getTokenFromStorage() {
        return persistentStorage
                .get(StorageKeys.OAUTH_TOKEN, OAuth2Token.class)
                .orElseThrow(
                        () -> new IllegalStateException(SessionError.SESSION_EXPIRED.exception()));
    }

    public CreatePaymentResponse createSepaPayment(CreatePaymentRequest request) {
        return client.request(Urls.SEPA_PAYMENT)
                .addBearerToken(getTokenFromStorage())
                .header(HeaderKeys.X_IBM_CLIENT_ID, getConfiguration().getClientId())
                .header(HeaderKeys.X_REQUEST_ID, Psd2Headers.getRequestId())
                .header(HeaderKeys.PSU_IP_ADDRESS, HeaderValues.PSU_IP_ADDRESS)
                .header(HeaderKeys.CONSENT_ID, persistentStorage.get(StorageKeys.CONSENT_ID))
                .body(request.toData(), MediaType.APPLICATION_JSON_TYPE)
                .post(CreatePaymentResponse.class);
    }

    public FetchPaymentResponse fetchPayment(String paymentId) {
        return client.request(
                        Urls.FETCH_PAYMENT
                                .parameter(IdTag.PAYMENT_PRODUCT, IdTag.SEPA_PAYMENT)
                                .parameter(IdTag.PAYMENT_ID, paymentId))
                .addBearerToken(getTokenFromStorage())
                .header(HeaderKeys.X_IBM_CLIENT_ID, getConfiguration().getClientId())
                .header(HeaderKeys.X_REQUEST_ID, Psd2Headers.getRequestId())
                .header(HeaderKeys.PSU_IP_ADDRESS, HeaderValues.PSU_IP_ADDRESS)
                .type(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .get(FetchPaymentResponse.class);
    }
}
