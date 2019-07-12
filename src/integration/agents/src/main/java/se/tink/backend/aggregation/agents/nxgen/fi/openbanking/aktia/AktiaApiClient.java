package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.AktiaConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.AktiaConstants.IdTags;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.AktiaConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.AktiaConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.AktiaConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.AktiaConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.authenticator.rpc.AuthorizeConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.configuration.AktiaConfiguration;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.executor.payment.rpc.CreatePaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.executor.payment.rpc.CreatePaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.executor.payment.rpc.GetPaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.fetcher.transactionalaccount.rpc.GetAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.fetcher.transactionalaccount.rpc.GetTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.authenticator.rpc.ConsentBaseRequest;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.date.ThreadSafeDateFormat;

public final class AktiaApiClient {
    private final TinkHttpClient client;
    private final PersistentStorage persistentStorage;
    private AktiaConfiguration configuration;

    public AktiaApiClient(TinkHttpClient client, PersistentStorage persistentStorage) {
        this.client = client;
        this.persistentStorage = persistentStorage;
    }

    public AktiaConfiguration getConfiguration() {
        return Optional.ofNullable(configuration)
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        AktiaConstants.ErrorMessages.MISSING_CONFIGURATION));
    }

    public void setConfiguration(AktiaConfiguration configuration) {
        this.configuration = configuration;
    }

    private RequestBuilder createRequest(URL url) {
        return client.request(url)
                .header(AktiaConstants.HeaderKeys.X_REQUEST_ID, UUID.randomUUID())
                .header(AktiaConstants.HeaderKeys.X_IBM_CLIENT_ID, configuration.getClientId())
                .header(
                        AktiaConstants.HeaderKeys.X_IBM_CLIENT_SECRET,
                        configuration.getClientSecret())
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON);
    }

    private RequestBuilder createRequestInSession(URL url) {
        return createRequest(url)
                .header(
                        AktiaConstants.HeaderKeys.CONSENT_ID,
                        persistentStorage.get(StorageKeys.CONSENT_ID));
    }

    public List<TransactionalAccount> getAccounts() {
        return createRequestInSession(AktiaConstants.Urls.GET_ACCOUNTS)
                .queryParam(AktiaConstants.QueryKeys.WITH_BALANCE, AktiaConstants.QueryValues.TRUE)
                .get(GetAccountsResponse.class)
                .toTinkAccounts();
    }

    public PaginatorResponse getTransactions(
            TransactionalAccount account, Date fromDate, Date toDate) {
        return createRequestInSession(
                        AktiaConstants.Urls.GET_TRANSACTIONS.parameter(
                                AktiaConstants.IdTags.ACCOUNT_ID, account.getApiIdentifier()))
                .queryParam(
                        AktiaConstants.QueryKeys.DATE_FROM,
                        ThreadSafeDateFormat.FORMATTER_DAILY.format(fromDate))
                .queryParam(
                        AktiaConstants.QueryKeys.DATE_TO,
                        ThreadSafeDateFormat.FORMATTER_DAILY.format(toDate))
                .queryParam(
                        AktiaConstants.QueryKeys.BOOKING_STATUS, AktiaConstants.QueryValues.BOTH)
                .get(GetTransactionsResponse.class);
    }

    public ConsentResponse createConsent(ConsentBaseRequest consentRequest, String state) {
        return createRequest(Urls.CREATE_CONSENT)
                .header(
                        HeaderKeys.TPP_REDIRECT_URI,
                        new URL(configuration.getRedirectUrl())
                                .queryParam(QueryKeys.STATE, state)
                                .queryParam(QueryKeys.CODE, QueryValues.CODE)
                                .get())
                .post(ConsentResponse.class, consentRequest);
    }

    public AuthorizeConsentResponse authorizeConsent(String startAuthorisation) {
        return createRequest(new URL(Urls.BASE_URL_AIS + startAuthorisation))
                .post(AuthorizeConsentResponse.class);
    }

    public CreatePaymentResponse createPayment(CreatePaymentRequest createPaymentRequest) {
        return createRequest(Urls.CREATE_PAYMENT)
                .header(HeaderKeys.TPP_REDIRECT_URI, configuration.getRedirectUrl())
                .post(CreatePaymentResponse.class, createPaymentRequest);
    }

    public GetPaymentResponse getPayment(String paymentId) {
        return createRequest(Urls.GET_PAYMENT.parameter(IdTags.PAYMENT_ID, paymentId))
                .get(GetPaymentResponse.class);
    }
}
