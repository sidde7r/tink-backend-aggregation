package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva;

import com.google.common.collect.ImmutableList;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.exceptions.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.BankServiceError;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.authenticator.rpc.LoginRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities.UserEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.creditcard.rpc.CreditCardTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.investment.rpc.SecurityProfitabilityRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.investment.rpc.SecurityProfitabilityResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.loan.rpc.LoanDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.transactionalaccount.entities.AccountContractsEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.transactionalaccount.entities.ContractEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.transactionalaccount.rpc.AccountTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.transactionalaccount.rpc.ProductsResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.transactionalaccount.rpc.TransactionsRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.rpc.FinancialDashboardResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.rpc.InitiateSessionResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.utils.BbvaUtils;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class BbvaApiClient {
    private static final Logger LOG = LoggerFactory.getLogger(BbvaApiClient.class);

    private final TinkHttpClient client;
    private final SessionStorage sessionStorage;
    private final String userAgent;

    public BbvaApiClient(TinkHttpClient client, SessionStorage sessionStorage) {
        this.client = client;
        this.sessionStorage = sessionStorage;
        this.userAgent =
                String.format(HeaderKeys.BBVA_USER_AGENT_VALUE, BbvaUtils.generateRandomHex());
    }

    private RequestBuilder createRequest(String url) {
        return client.request(url)
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON);
    }

    private RequestBuilder createRequestInSession(String url) {
        return createRequest(url)
                .header(HeaderKeys.ORIGIN_KEY, HeaderKeys.ORIGIN_VALUE)
                .header(HeaderKeys.REFERER_KEY, HeaderKeys.REFERER_VALUE)
                .header(HeaderKeys.TSEC_KEY, getTsec())
                .header(HeaderKeys.BBVA_USER_AGENT_KEY, getUserAgent());
    }

    public HttpResponse login(LoginRequest loginRequest) {
        return client.request(BbvaConstants.Url.LOGIN)
                .type(HeaderKeys.CONTENT_TYPE_URLENCODED_UTF8)
                .accept(MediaType.WILDCARD)
                .header(HeaderKeys.CONSUMER_ID_KEY, HeaderKeys.CONSUMER_ID_VALUE)
                .header(HeaderKeys.BBVA_USER_AGENT_KEY, getUserAgent())
                .post(HttpResponse.class, loginRequest);
    }

    public void logout() {
        createRequest(BbvaConstants.Url.SESSION)
                .header(HeaderKeys.BBVA_USER_AGENT_KEY, getUserAgent())
                .delete();
    }

    public FinancialDashboardResponse fetchFinancialDashboard() {
        return createRequestInSession(BbvaConstants.Url.FINANCIAL_DASHBOARD)
                .queryParam(QueryKeys.DASHBOARD_CUSTOMER_ID, getUserId())
                .get(FinancialDashboardResponse.class);
    }

    public ProductsResponse fetchProducts() {
        return createRequestInSession(BbvaConstants.Url.PRODUCTS).get(ProductsResponse.class);
    }

    public AccountTransactionsResponse fetchAccountTransactions(Account account, int keyIndex) {
        final TransactionsRequest request = createAccountTransactionsQuery(account);

        return createRequestInSession(BbvaConstants.Url.ACCOUNT_TRANSACTION)
                .queryParam(QueryKeys.PAGINATION_OFFSET, String.valueOf(keyIndex))
                .queryParam(QueryKeys.PAGE_SIZE, String.valueOf(BbvaConstants.PAGE_SIZE))
                .post(AccountTransactionsResponse.class, request);
    }

    public CreditCardTransactionsResponse fetchCreditCardTransactions(
            Account account, String keyIndex) {
        return createRequestInSession(BbvaConstants.Url.CREDIT_CARD_TRANSACTIONS)
                .queryParam(
                        QueryKeys.CONTRACT_ID,
                        account.getFromTemporaryStorage(BbvaConstants.StorageKeys.ACCOUNT_ID))
                .queryParam(
                        QueryKeys.CARD_TRANSACTION_TYPE,
                        BbvaConstants.AccountType.CREDIT_CARD_SHORT_TYPE)
                .queryParam(QueryKeys.PAGINATION_OFFSET, keyIndex)
                .get(CreditCardTransactionsResponse.class);
    }

    public SecurityProfitabilityResponse fetchSecurityProfitability(
            String portfolioId, String securityCode) {
        final SecurityProfitabilityRequest request =
                SecurityProfitabilityRequest.create(portfolioId, securityCode);

        return createRequestInSession(BbvaConstants.Url.SECURITY_PROFITABILITY)
                .post(SecurityProfitabilityResponse.class, request);
    }

    public LoanDetailsResponse fetchLoanDetails(String loanId) {
        final String url =
                new URL(BbvaConstants.Url.LOAN_DETAILS)
                        .parameter(BbvaConstants.Url.PARAM_ID, loanId)
                        .get();

        return createRequestInSession(url).get(LoanDetailsResponse.class);
    }

    public TransactionsRequest createAccountTransactionsQuery(Account account) {
        final String accountId = account.getFromTemporaryStorage(BbvaConstants.StorageKeys.ACCOUNT_ID);
        AccountContractsEntity accountContract = new AccountContractsEntity();
        accountContract.setContract(new ContractEntity().setId(accountId));

        final TransactionsRequest request = new TransactionsRequest();
        request.setCustomer(new UserEntity(getUserId()));
        request.setSearchType(BbvaConstants.PostParameter.SEARCH_TYPE);
        request.setAccountContracts(ImmutableList.of(accountContract));

        return request;
    }

    public InitiateSessionResponse initiateSession() throws SessionException, BankServiceException {
        Map<String, String> body = new HashMap<>();
        body.put(
                BbvaConstants.PostParameter.CONSUMER_ID_KEY,
                BbvaConstants.PostParameter.CONSUMER_ID_VALUE);

        HttpResponse response =
                createRequest(BbvaConstants.Url.SESSION)
                        .header(HeaderKeys.BBVA_USER_AGENT_KEY, getUserAgent())
                        .post(HttpResponse.class, body);

        if (MediaType.TEXT_HTML.equalsIgnoreCase(
                response.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE))) {
            throw SessionError.SESSION_EXPIRED.exception();
        }

        setTsec(response.getHeaders().getFirst(HeaderKeys.TSEC_KEY));

        InitiateSessionResponse initiateSessionResponse =
                response.getBody(InitiateSessionResponse.class);

        if (initiateSessionResponse.hasError()) {
            if (initiateSessionResponse.hasError(BbvaConstants.Error.BANK_SERVICE_UNAVAILABLE)) {
                throw BankServiceError.NO_BANK_SERVICE.exception();
            }

            LOG.warn(
                    String.format(
                            "Bank responded with error: %s",
                            SerializationUtils.serializeToString(
                                    initiateSessionResponse.getResult())));

            throw new IllegalStateException("Failed to initiate session");
        }

        setUserId(initiateSessionResponse.getUser().getId());

        return initiateSessionResponse;
    }

    private String getUserAgent() {
        return userAgent;
    }

    public String getTsec() {
        return sessionStorage.get(BbvaConstants.StorageKeys.TSEC);
    }

    private void setTsec(String tsec) {
        sessionStorage.put(BbvaConstants.StorageKeys.TSEC, tsec);
    }

    public String getUserId() {
        return sessionStorage.get(BbvaConstants.StorageKeys.USER_ID);
    }

    public void setUserId(String userId) {
        sessionStorage.put(BbvaConstants.StorageKeys.USER_ID, userId);
    }
}
