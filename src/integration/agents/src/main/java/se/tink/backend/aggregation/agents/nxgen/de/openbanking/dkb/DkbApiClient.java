package se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb;

import java.util.Date;
import java.util.NoSuchElementException;
import java.util.UUID;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.DkbConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.DkbConstants.IdTags;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.DkbConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.DkbConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.DkbConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.fetcher.transactionalaccount.rpc.GetAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.fetcher.transactionalaccount.rpc.GetBalancesResponse;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.fetcher.transactionalaccount.rpc.GetTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.payments.rpc.CreatePaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.payments.rpc.CreatePaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.payments.rpc.FetchPaymentResponse;
import se.tink.backend.aggregation.api.Psd2Headers;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.date.ThreadSafeDateFormat;

public final class DkbApiClient {

    private final TinkHttpClient client;
    private final DkbStorage storage;

    public DkbApiClient(TinkHttpClient client, DkbStorage storage) {
        this.client = client;
        this.storage = storage;
    }

    private RequestBuilder createRequest(URL url) {
        return client.request(url)
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON);
    }

    private RequestBuilder createRequestInSession(URL url) {
        return createRequest(url)
                .addBearerToken(storage.getWso2OAuthToken())
                .header("PSD2-AUTHORIZATION", storage.getAccessToken().toAuthorizeHeader());
    }

    private RequestBuilder createFetchingRequest(URL url) {
        return createRequestInSession(url)
                .header(HeaderKeys.X_REQUEST_ID, UUID.randomUUID())
                .header(HeaderKeys.CONSENT_ID, getConsent());
    }

    private String getConsent() {
        return storage.getConsentId()
                .orElseThrow(() -> new NoSuchElementException("Can't obtain consent id"));
    }

    public GetAccountsResponse getAccounts() {
        return createFetchingRequest(Urls.GET_ACCOUNTS).get(GetAccountsResponse.class);
    }

    public GetBalancesResponse getBalances(String accountId) {
        return createFetchingRequest(Urls.GET_BALANCES.parameter(IdTags.ACCOUNT_ID, accountId))
                .get(GetBalancesResponse.class);
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
                .header(HeaderKeys.X_REQUEST_ID, Psd2Headers.getRequestId())
                .post(CreatePaymentResponse.class, createPaymentRequest);
    }

    public FetchPaymentResponse getPayment(String paymentId, String paymentProduct)
            throws HttpResponseException {
        return createRequestInSession(
                        Urls.FETCH_PAYMENT
                                .parameter(IdTags.PAYMENT_PRODUCT, paymentProduct)
                                .parameter(IdTags.PAYMENT_ID, paymentId))
                .header(HeaderKeys.X_REQUEST_ID, Psd2Headers.getRequestId())
                .get(FetchPaymentResponse.class);
    }
}
