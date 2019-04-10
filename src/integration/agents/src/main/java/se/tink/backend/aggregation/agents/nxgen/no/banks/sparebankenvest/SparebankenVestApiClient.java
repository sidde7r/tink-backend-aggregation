package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest;

import java.util.Date;
import java.util.Objects;
import javax.ws.rs.core.MediaType;
import org.apache.http.cookie.Cookie;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.authenticator.entities.SecurityParamsRequestBody;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.fetcher.creditcard.entities.BankIdentifier;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.fetcher.creditcard.rpc.CreditCardTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.fetcher.creditcard.rpc.FetchCreditCardsResponse;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.fetcher.investment.rpc.FetchFundInvestmentsResponse;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.fetcher.loan.entities.LoanEntity;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.fetcher.loan.rpc.FetchLoansResponse;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.fetcher.loan.rpc.LoanDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.fetcher.transactionalaccount.entities.AccountsListResponse;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.fetcher.transactionalaccount.entities.DuePaymentsResponse;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.fetcher.transactionalaccount.entities.TransactionsListResponse;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;

public class SparebankenVestApiClient {
    private final TinkHttpClient client;

    public SparebankenVestApiClient(TinkHttpClient client) {
        this.client = client;
    }

    public void initLogin() {
        this.client
                .request(SparebankenVestConstants.Urls.LOGIN)
                .queryParam(
                        SparebankenVestConstants.QueryParams.NO_CACHE_KEY,
                        SparebankenVestConstants.QueryParams.NO_CACHE_VALUE)
                .get(HttpResponse.class);
    }

    public String activate(String securityToken) {
        return this.client
                .request(SparebankenVestConstants.Urls.AUTHENTICATE)
                .queryParam(SparebankenVestConstants.QueryParams.SO_KEY, securityToken)
                .queryParam(
                        SparebankenVestConstants.QueryParams.IS_NEW_ACTIVATION_KEY,
                        SparebankenVestConstants.QueryParams.IS_NEW_ACTIVATION_VALUE)
                .cookie(
                        SparebankenVestConstants.Headers.MOBILE_NAME_COOKIE_KEY,
                        SparebankenVestConstants.Headers.MOBILE_NAME_COOKIE_VALUE)
                .get(String.class);
    }

    public String authenticate(String securityToken, String hardwareId) {
        return this.client
                .request(SparebankenVestConstants.Urls.AUTHENTICATE)
                .queryParam(SparebankenVestConstants.QueryParams.SO_KEY, securityToken)
                .queryParam(SparebankenVestConstants.QueryParams.HARDWARE_ID_KEY, hardwareId)
                .get(String.class);
    }

    public String postSecurityParamsActivation(SecurityParamsRequestBody loginRequestBody) {
        return getPostSecurityParamsRequest()
                .cookie(
                        SparebankenVestConstants.Headers.MOBILE_NAME_COOKIE_KEY,
                        SparebankenVestConstants.Headers.MOBILE_NAME_COOKIE_VALUE)
                .post(String.class, loginRequestBody);
    }

    public String postSecurityParamsAuthentication(SecurityParamsRequestBody loginRequestBody) {
        return getPostSecurityParamsRequest().post(String.class, loginRequestBody);
    }

    public void finalizeLogin(SecurityParamsRequestBody loginRequestBody) {
        getRequest(SparebankenVestConstants.Urls.LOGIN)
                .header(
                        SparebankenVestConstants.Headers.ORIGIN_KEY,
                        SparebankenVestConstants.Urls.HOST_SECURITY)
                .post(HttpResponse.class, loginRequestBody);
    }

    public AccountsListResponse fetchAccounts() {
        return getRequest(SparebankenVestConstants.Urls.ACCOUNTS).get(AccountsListResponse.class);
    }

    public TransactionsListResponse fetchTransactions(String accountId, String range) {
        return getRequest(SparebankenVestConstants.Urls.TRANSACTIONS)
                .queryParam(SparebankenVestConstants.QueryParams.ACCOUNT_NUMBER_KEY, accountId)
                .header(SparebankenVestConstants.Headers.RANGE_KEY, range)
                .get(TransactionsListResponse.class);
    }

    public DuePaymentsResponse fetchUpcomingTransactions() {
        return getRequest(SparebankenVestConstants.Urls.DUE_PAYMENTS)
                .header(SparebankenVestConstants.Headers.XCSRF_TOKEN, getCsrfTokenCookieValue())
                .get(DuePaymentsResponse.class);
    }

    public FetchLoansResponse fetchLoans() {
        return getRequest(SparebankenVestConstants.Urls.LOANS).get(FetchLoansResponse.class);
    }

    public FetchCreditCardsResponse fetchCreditCardAccounts() {
        return getRequest(SparebankenVestConstants.Urls.CREDIT_CARD_ACCOUNTS)
                .get(FetchCreditCardsResponse.class);
    }

    public LoanDetailsResponse fetchLoanDetails(LoanEntity loanEntity) {
        return getRequest(
                        SparebankenVestConstants.Urls.LOAN_DETAILS
                                .parameter(
                                        SparebankenVestConstants.Urls.LOAN_TYPE_PARAM,
                                        String.valueOf(loanEntity.getType()))
                                .parameter(
                                        SparebankenVestConstants.Urls.LOAN_NUMBER_GUID_PARAM,
                                        String.valueOf(loanEntity.getLoanNumberGuid())))
                .get(LoanDetailsResponse.class);
    }

    public String fetchCurrencyLoanDetails(LoanEntity loanEntity) {
        return getRequest(
                        SparebankenVestConstants.Urls.CURRENCY_LOAN_DETAILS.parameter(
                                SparebankenVestConstants.Urls.LOAN_NUMBER_GUID_PARAM,
                                String.valueOf(loanEntity.getLoanNumberGuid())))
                .get(String.class);
    }

    public CreditCardTransactionsResponse fetchCreditCardTransactions(
            BankIdentifier bankIdentifier, Date fromDate, Date toDate, int startIndex) {
        int endIndex =
                startIndex + SparebankenVestConstants.PagePagination.MAX_TRANSACTIONS_IN_BATCH - 1;

        return getRequest(SparebankenVestConstants.Urls.CREDIT_CARD_TRANSACTIONS)
                .queryParam(
                        SparebankenVestConstants.QueryParams.CARD_NUMBER_GUID_KEY,
                        bankIdentifier.getCardNumberGuid())
                .queryParam(
                        SparebankenVestConstants.QueryParams.KID_GUID_KEY,
                        bankIdentifier.getKidGuid())
                .queryParam(
                        SparebankenVestConstants.QueryParams.FROM_DATE_KEY,
                        jsonFormatDate(fromDate))
                .queryParam(
                        SparebankenVestConstants.QueryParams.TO_DATE_KEY, jsonFormatDate(toDate))
                .header(
                        SparebankenVestConstants.Headers.RANGE_KEY,
                        String.format("items=%d-%d", startIndex, endIndex))
                .get(CreditCardTransactionsResponse.class);
    }

    private static String jsonFormatDate(Date date) {
        return SparebankenVestConstants.QueryParams.DATE_FORMATTER.format(date);
    }

    public FetchFundInvestmentsResponse fetchInvestments() {
        return getRequest(SparebankenVestConstants.Urls.INVESTMENTS)
                .get(FetchFundInvestmentsResponse.class);
    }

    public boolean keepAlive() {
        return this.client
                .request(SparebankenVestConstants.Urls.KEEP_ALIVE)
                .queryParam(
                        SparebankenVestConstants.QueryParams.PREVENT_CACHE_KEY,
                        String.valueOf(System.currentTimeMillis()))
                .get(boolean.class);
    }

    private RequestBuilder getPostSecurityParamsRequest() {
        return this.client
                .request(SparebankenVestConstants.Urls.STS_PRIVATE_WEB)
                .header(
                        SparebankenVestConstants.Headers.ORIGIN_KEY,
                        SparebankenVestConstants.Urls.HOST_SECURITY);
    }

    private RequestBuilder getRequest(URL url) {
        return this.client.request(url).type(MediaType.APPLICATION_FORM_URLENCODED);
    }

    private String getCsrfTokenCookieValue() {
        return this.client.getCookies().stream()
                .filter(
                        cookie ->
                                Objects.equals(
                                        cookie.getName().toLowerCase(),
                                        SparebankenVestConstants.Cookies.CSRFTOKEN))
                .findFirst()
                .map(Cookie::getValue)
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        "CsrfToken is not found, it's needed for fetching upcoming transactions."));
    }
}
