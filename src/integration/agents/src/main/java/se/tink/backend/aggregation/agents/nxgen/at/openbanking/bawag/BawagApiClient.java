package se.tink.backend.aggregation.agents.nxgen.at.openbanking.bawag;

import java.util.Date;
import java.util.UUID;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.bawag.BawagConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.bawag.BawagConstants.HeaderValues;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.bawag.BawagConstants.IdTags;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.bawag.BawagConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.bawag.BawagConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.bawag.BawagConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.bawag.BawagConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.bawag.authenticator.entity.ConsentBaseRequest;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.bawag.authenticator.entity.ConsentBaseResponse;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.bawag.configuration.BawagConfiguration;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.bawag.executor.payment.rpc.CreatePaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.bawag.executor.payment.rpc.CreatePaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.bawag.executor.payment.rpc.FetchPaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.bawag.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.bawag.fetcher.transactionalaccount.rpc.GetAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.bawag.fetcher.transactionalaccount.rpc.GetBalancesResponse;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.bawag.fetcher.transactionalaccount.rpc.GetTransactionsResponse;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.date.ThreadSafeDateFormat;

public final class BawagApiClient {

    private final TinkHttpClient client;
    private final PersistentStorage persistentStorage;
    private BawagConfiguration configuration;
    private String redirectUrl;

    public BawagApiClient(TinkHttpClient client, PersistentStorage persistentStorage) {
        this.client = client;
        this.persistentStorage = persistentStorage;
    }

    protected void setConfiguration(AgentConfiguration<BawagConfiguration> agentConfiguration) {
        this.configuration = agentConfiguration.getClientConfiguration();
        this.redirectUrl = agentConfiguration.getRedirectUrl();
    }

    private RequestBuilder createRequest(URL url) {
        return client.request(url)
                .header(HeaderKeys.X_REQUEST_ID, UUID.randomUUID())
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON);
    }

    private RequestBuilder createRequestInSession(URL url) {
        return createRequest(url)
                .header(HeaderKeys.CONSENT_ID, persistentStorage.get(StorageKeys.CONSENT_ID));
    }

    public ConsentBaseResponse createConsent(ConsentBaseRequest body, String state) {
        return createRequest(Urls.CREATE_CONSENT)
                .header(HeaderKeys.PSU_IP_ADDRESS, HeaderValues.PSU_IP_ADDRESS)
                .header(HeaderKeys.TPP_REDIRECT_PREFERRED, HeaderValues.TRUE)
                .header(
                        HeaderKeys.TPP_REDIRECT_URI,
                        new URL(redirectUrl)
                                .queryParam(QueryKeys.STATE, state)
                                .queryParam(QueryKeys.CODE, QueryValues.CODE)
                                .get())
                .post(ConsentBaseResponse.class, body);
    }

    public GetAccountsResponse getAccounts() {
        return createRequestInSession(Urls.GET_ACCOUNTS)
                .queryParam(QueryKeys.WITH_BALANCE, QueryValues.TRUE)
                .get(GetAccountsResponse.class);
    }

    public GetBalancesResponse getBalances(AccountEntity account) {
        return createRequestInSession(
                        Urls.GET_BALANCES.parameter(IdTags.ACCOUNT_ID, account.getResourceId()))
                .get(GetBalancesResponse.class);
    }

    public GetTransactionsResponse getTransactions(
            TransactionalAccount account, Date fromDate, Date toDate) {
        return createRequestInSession(
                        Urls.GET_TRANSACTIONS.parameter(
                                IdTags.ACCOUNT_ID, account.getApiIdentifier()))
                .queryParam(QueryKeys.BOOKING_STATUS, QueryValues.BOOTH)
                .queryParam(
                        QueryKeys.DATE_FROM, ThreadSafeDateFormat.FORMATTER_DAILY.format(fromDate))
                .queryParam(QueryKeys.DATE_TO, ThreadSafeDateFormat.FORMATTER_DAILY.format(toDate))
                .get(GetTransactionsResponse.class);
    }

    public CreatePaymentResponse createPayment(CreatePaymentRequest paymentRequest) {
        URL url =
                paymentRequest.isSepa()
                        ? Urls.CREATE_SEPA_TRANSFER
                        : Urls.CREATE_CROSS_BORDER_TRANSFER;
        return createRequest(url)
                .body(paymentRequest)
                .header(HeaderKeys.PSU_IP_ADDRESS, HeaderValues.PSU_IP_ADDRESS)
                .post(CreatePaymentResponse.class);
    }

    public FetchPaymentResponse fetchPayment(PaymentRequest paymentRequest) {
        URL url =
                paymentRequest.getPayment().isSepa()
                        ? Urls.GET_SEPA_TRANSFER
                        : Urls.GET_CROSS_BORDER_TRANSFER;
        return createRequest(
                        url.parameter(IdTags.PAYMENT_ID, paymentRequest.getPayment().getUniqueId()))
                .header(HeaderKeys.PSU_IP_ADDRESS, HeaderValues.PSU_IP_ADDRESS)
                .get(FetchPaymentResponse.class);
    }
}
