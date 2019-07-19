package se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.DkbConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.DkbConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.DkbConstants.IdTags;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.DkbConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.DkbConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.DkbConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.DkbConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.authenticator.rpc.GetConsentRequest;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.authenticator.rpc.GetConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.authenticator.rpc.GetTokenForm;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.authenticator.rpc.GetTokenResponse;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.configuration.DkbConfiguration;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.fetcher.transactionalaccount.rpc.GetAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.fetcher.transactionalaccount.rpc.GetTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.payments.rpc.CreatePaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.payments.rpc.CreatePaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.payments.rpc.FetchPaymentResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.date.ThreadSafeDateFormat;

public final class DkbApiClient {

    private final TinkHttpClient client;
    private final PersistentStorage persistentStorage;
    private DkbConfiguration configuration;

    public DkbApiClient(TinkHttpClient client, PersistentStorage persistentStorage) {
        this.client = client;
        this.persistentStorage = persistentStorage;
    }

    private DkbConfiguration getConfiguration() {
        return Optional.ofNullable(configuration)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_CONFIGURATION));
    }

    protected void setConfiguration(DkbConfiguration configuration) {
        this.configuration = configuration;
    }

    private RequestBuilder createRequest(URL url) {
        return client.request(url)
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON);
    }

    private RequestBuilder createRequestInSession(URL url) {
        final OAuth2Token authToken = getTokenFromStorage();

        return createRequest(url).addBearerToken(authToken);
    }

    private RequestBuilder createFetchingRequest(URL url) {
        return createRequestInSession(url)
                .header(HeaderKeys.X_REQUEST_ID, UUID.randomUUID())
                .header(HeaderKeys.CONSENT_ID, getConsentFromStorage());
    }

    private String getConsentFromStorage() {
        return persistentStorage.get(StorageKeys.CONSENT_ID);
    }

    private OAuth2Token getTokenFromStorage() {
        return persistentStorage
                .get(StorageKeys.OAUTH_TOKEN, OAuth2Token.class)
                .orElseThrow(
                        () -> new IllegalStateException(SessionError.SESSION_EXPIRED.exception()));
    }

    public GetTokenResponse authenticate(
            String clientId, String clientSecret, GetTokenForm getTokenForm) {
        return createRequest(Urls.TOKEN)
                .addBasicAuth(clientId, clientSecret)
                .body(getTokenForm, MediaType.APPLICATION_FORM_URLENCODED)
                .post(GetTokenResponse.class);
    }

    public GetConsentResponse getConsent(GetConsentRequest getConsentRequest) {
        return createRequestInSession(Urls.CONSENT)
                .header(HeaderKeys.X_REQUEST_ID, UUID.randomUUID())
                .post(GetConsentResponse.class, getConsentRequest);
    }

    public GetAccountsResponse getAccounts() {
        return createFetchingRequest(Urls.GET_ACCOUNTS).get(GetAccountsResponse.class);
    }

    public GetTransactionsResponse getTransactions(
            TransactionalAccount account, Date fromDate, Date toDate) {
        return createFetchingRequest(
                        Urls.GET_TRANSACTIONS.parameter(
                                IdTags.ACCOUNT_ID, account.getApiIdentifier()))
                .queryParam(
                        QueryKeys.DATE_FROM, ThreadSafeDateFormat.FORMATTER_DAILY.format(fromDate))
                .queryParam(QueryKeys.DATE_TO, ThreadSafeDateFormat.FORMATTER_DAILY.format(toDate))
                .queryParam(QueryKeys.BOOKING_STATUS, QueryValues.BOTH)
                .get(GetTransactionsResponse.class);
    }

    public CreatePaymentResponse createPayment(
            CreatePaymentRequest createPaymentRequest, String paymentProduct)
            throws HttpResponseException {
        return createRequestInSession(
                        Urls.CREATE_PAYMENT.parameter(IdTags.PAYMENT_PRODUCT, paymentProduct))
                .header(HeaderKeys.X_REQUEST_ID, getRequestId())
                .header(HeaderKeys.PSU_IP_ADDRESS, getPsuIpAddress())
                .post(CreatePaymentResponse.class, createPaymentRequest);
    }

    public FetchPaymentResponse getPayment(String paymentId, String paymentProduct)
            throws HttpResponseException {
        return createRequestInSession(
                        Urls.FETCH_PAYMENT
                                .parameter(IdTags.PAYMENT_PRODUCT, paymentProduct)
                                .parameter(IdTags.PAYMENT_ID, paymentId))
                .header(HeaderKeys.X_REQUEST_ID, getRequestId())
                .header(HeaderKeys.PSU_IP_ADDRESS, getPsuIpAddress())
                .get(FetchPaymentResponse.class);
    }

    private String getPsuIpAddress() {
        // This is supposed to be the IP address of the PSU, but we can't supply it on sandbox so we
        // use dummy value
        return "82.117.210.2"; //
    }

    private String getRequestId() {
        return UUID.randomUUID().toString();
    }
}
