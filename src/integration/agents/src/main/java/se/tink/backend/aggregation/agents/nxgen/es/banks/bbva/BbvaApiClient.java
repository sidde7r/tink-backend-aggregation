package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva;

import com.google.common.collect.ImmutableList;
import javax.ws.rs.core.MediaType;
import org.assertj.core.util.Strings;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaConstants.Fetchers;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaConstants.Headers;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaConstants.Url;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.authenticator.rpc.LoginRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.authenticator.rpc.LoginResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities.UserEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.creditcard.rpc.CreditCardTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.identitydata.rpc.IdentityDataResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.investment.rpc.FinancialInvestmentRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.investment.rpc.FinancialInvestmentResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.investment.rpc.HistoricalDateRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.investment.rpc.HistoricalDateResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.loan.rpc.LoanDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.transactionalaccount.entities.AccountContractsEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.transactionalaccount.entities.ContractEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.transactionalaccount.rpc.AccountTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.transactionalaccount.rpc.TransactionsRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.rpc.FinancialDashboardResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.utils.BbvaUtils;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class BbvaApiClient {

    private final TinkHttpClient client;
    private final SessionStorage sessionStorage;
    private final String userAgent;

    public BbvaApiClient(TinkHttpClient client, SessionStorage sessionStorage) {
        this.client = client;
        this.sessionStorage = sessionStorage;
        this.userAgent =
                String.format(Headers.BBVA_USER_AGENT.getValue(), BbvaUtils.generateRandomHex());
    }

    private RequestBuilder createRequest(String url) {
        return client.request(url)
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON);
    }

    private RequestBuilder createRequestInSession(String url) {
        return createRequest(url)
                .header(Headers.REFERER)
                .header(HeaderKeys.TSEC_KEY, getTsec())
                .header(Headers.BBVA_USER_AGENT.getKey(), getUserAgent());
    }

    public LoginResponse login(LoginRequest loginRequest) {
        HttpResponse httpResponse =
                client.request(BbvaConstants.Url.TICKET)
                        .type(MediaType.APPLICATION_JSON_TYPE)
                        .accept(MediaType.APPLICATION_JSON_TYPE)
                        .header(Headers.CONSUMER_ID)
                        .header(Headers.BBVA_USER_AGENT.getKey(), getUserAgent())
                        .post(HttpResponse.class, loginRequest);

        LoginResponse loginResponse = httpResponse.getBody(LoginResponse.class);

        setTsec(httpResponse.getHeaders().getFirst(HeaderKeys.TSEC_KEY));
        setUserId(loginResponse.getUser().getId());
        return loginResponse;
    }

    public void logout() {
        createRequest(BbvaConstants.Url.TICKET)
                .header(Headers.BBVA_USER_AGENT.getKey(), getUserAgent())
                .header(HeaderKeys.TSEC_KEY, getTsec())
                .delete();
    }

    public FinancialDashboardResponse fetchFinancialDashboard() {
        return createRequestInSession(BbvaConstants.Url.FINANCIAL_DASHBOARD)
                .queryParam(QueryKeys.DASHBOARD_CUSTOMER_ID, getUserId())
                .queryParam(QueryKeys.DASHBOARD_FILTER, QueryValues.DASHBOARD_FILTER)
                .get(FinancialDashboardResponse.class);
    }

    public AccountTransactionsResponse fetchAccountTransactions(Account account, String pageKey) {
        final TransactionsRequest request = createAccountTransactionsQuery(account);

        final RequestBuilder builder;
        if (Strings.isNullOrEmpty(pageKey)) {
            // First page
            builder =
                    createRequestInSession(BbvaConstants.Url.ACCOUNT_TRANSACTION)
                            .queryParam(QueryKeys.PAGINATION_OFFSET, QueryValues.FIRST_PAGE_KEY)
                            .queryParam(QueryKeys.PAGE_SIZE, String.valueOf(Fetchers.PAGE_SIZE));
        } else {
            // Pagination key is complete URL from /ASO
            builder = createRequestInSession(Url.ASO + pageKey);
        }
        return builder.post(AccountTransactionsResponse.class, request);
    }

    public CreditCardTransactionsResponse fetchCreditCardTransactions(
            Account account, String keyIndex) {
        return createRequestInSession(BbvaConstants.Url.CREDIT_CARD_TRANSACTIONS)
                .queryParam(QueryKeys.CONTRACT_ID, account.getApiIdentifier())
                .queryParam(
                        QueryKeys.CARD_TRANSACTION_TYPE,
                        BbvaConstants.AccountType.CREDIT_CARD_SHORT_TYPE)
                .queryParam(QueryKeys.PAGINATION_OFFSET, keyIndex)
                .get(CreditCardTransactionsResponse.class);
    }

    public FinancialInvestmentResponse fetchFinancialInvestment(
            FinancialInvestmentRequest request) {
        return createRequestInSession(BbvaConstants.Url.FINANCIAL_INVESTMENTS)
                .post(FinancialInvestmentResponse.class, request);
    }

    public HistoricalDateResponse fetchInvestmentHistoricalDate(HistoricalDateRequest request) {
        return createRequestInSession(BbvaConstants.Url.HISTORICAL_DATE)
                .post(HistoricalDateResponse.class, request);
    }

    public LoanDetailsResponse fetchLoanDetails(String loanId) {
        final String url =
                new URL(BbvaConstants.Url.LOAN_DETAILS)
                        .parameter(BbvaConstants.Url.PARAM_ID, loanId)
                        .get();

        return createRequestInSession(url).get(LoanDetailsResponse.class);
    }

    public TransactionsRequest createAccountTransactionsQuery(Account account) {
        final String accountId = account.getApiIdentifier();
        AccountContractsEntity accountContract = new AccountContractsEntity();
        accountContract.setContract(new ContractEntity().setId(accountId));

        final TransactionsRequest request = new TransactionsRequest();
        request.setCustomer(new UserEntity(getUserId()));
        request.setSearchType(BbvaConstants.PostParameter.SEARCH_TYPE);
        request.setAccountContracts(ImmutableList.of(accountContract));

        return request;
    }

    public IdentityDataResponse fetchIdentityData() {
        final String url =
                new URL(BbvaConstants.Url.IDENTITY_DATA)
                        .parameter(BbvaConstants.Url.PARAM_ID, getUserId())
                        .get();

        return createRequestInSession(url).get(IdentityDataResponse.class);
    }

    private String getUserAgent() {
        return userAgent;
    }

    private String getTsec() {
        return sessionStorage.get(BbvaConstants.StorageKeys.TSEC);
    }

    private void setTsec(String tsec) {
        sessionStorage.put(BbvaConstants.StorageKeys.TSEC, tsec);
    }

    public String getUserId() {
        return sessionStorage.get(BbvaConstants.StorageKeys.USER_ID);
    }

    private void setIdTypeCode(String idTypeCode) {
        sessionStorage.put(BbvaConstants.StorageKeys.ID_TYPE_CODE, idTypeCode);
    }

    public void setUserId(String userId) {
        sessionStorage.put(BbvaConstants.StorageKeys.USER_ID, userId);
    }
}
