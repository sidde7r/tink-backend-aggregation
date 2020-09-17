package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest;

import com.google.common.base.Preconditions;
import java.util.Date;
import java.util.Objects;
import javax.ws.rs.core.MediaType;
import org.apache.http.cookie.Cookie;
import org.assertj.core.util.DateUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.authenticator.entities.SecurityParamsRequestBody;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.fetcher.creditcard.entities.ApiIdentifier;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.fetcher.creditcard.rpc.CreditCardTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.fetcher.creditcard.rpc.FetchCreditCardsResponse;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.fetcher.creditcard.rpc.InnloggetRequest;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.fetcher.investment.rpc.FetchFundInvestmentsResponse;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.fetcher.loan.entities.LoanEntity;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.fetcher.loan.rpc.FetchLoansResponse;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.fetcher.loan.rpc.LoanDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.fetcher.transactionalaccount.rpc.AccountListResponse;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.fetcher.transactionalaccount.rpc.DuePaymentsResponse;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.fetcher.transactionalaccount.rpc.TransactionsListResponse;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.fetcher.transactionalaccount.rpc.TransactionsRequestBody;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class SparebankenVestApiClient {
    private final TinkHttpClient client;

    public SparebankenVestApiClient(TinkHttpClient client) {
        this.client = client;
        this.client.disableSignatureRequestHeader();
    }

    public void initLogin() {
        this.client
                .request(SparebankenVestConstants.Urls.INIT)
                .header(
                        SparebankenVestConstants.Headers.ACCEPT,
                        SparebankenVestConstants.Headers.TEXT_HTML_APPLICATION_XHTML_XML)
                .get(HttpResponse.class);
    }

    public String activateNew() {
        return this.client
                .request(SparebankenVestConstants.Urls.LOGIN_NEW)
                .header(
                        SparebankenVestConstants.Headers.ACCEPT,
                        SparebankenVestConstants.Headers.TEXT_HTML_APPLICATION_XHTML_XML)
                .get(String.class);
    }

    public String customerIdAct() {
        return this.client
                .request(SparebankenVestConstants.Urls.CUSTOMER_ID_ACT)
                .header(
                        SparebankenVestConstants.Headers.ACCEPT,
                        SparebankenVestConstants.Headers.TEXT_HTML_APPLICATION_XHTML_XML)
                .get(String.class);
    }

    public String getActivationCode() {
        return this.client
                .request(SparebankenVestConstants.Urls.GET_ACTIVATION_CODE)
                .header(
                        SparebankenVestConstants.Headers.ACCEPT,
                        SparebankenVestConstants.Headers.TEXT_HTML_APPLICATION_XHTML_XML)
                .get(String.class);
    }

    public String getMobileName() {
        return this.client
                .request(SparebankenVestConstants.Urls.GET_MOBILE_NAME)
                .header(
                        SparebankenVestConstants.Headers.ACCEPT,
                        SparebankenVestConstants.Headers.TEXT_HTML_APPLICATION_XHTML_XML)
                .get(String.class);
    }

    public String choosePin() {
        return this.client
                .request(SparebankenVestConstants.Urls.CHOOSE_PIN)
                .header(
                        SparebankenVestConstants.Headers.ACCEPT,
                        SparebankenVestConstants.Headers.TEXT_HTML_APPLICATION_XHTML_XML)
                .cookie(
                        SparebankenVestConstants.Headers.MOBILE_NAME_COOKIE_KEY,
                        SparebankenVestConstants.Headers.MOBILE_NAME_COOKIE_VALUE)
                .get(String.class);
    }

    public void preRegistration() {
        activateNew();
        customerIdAct();
        getActivationCode();
        getMobileName();
        choosePin();
    }

    public String activate(String securityToken) {
        return this.client
                .request(SparebankenVestConstants.Urls.AUTHENTICATE)
                .header(
                        SparebankenVestConstants.Headers.ACCEPT,
                        SparebankenVestConstants.Headers.TEXT_HTML_APPLICATION_XHTML_XML)
                .header(
                        SparebankenVestConstants.Headers.REFERER,
                        SparebankenVestConstants.Headers.REFERER_VALUE)
                .cookie(
                        SparebankenVestConstants.Headers.MOBILE_NAME_COOKIE_KEY,
                        SparebankenVestConstants.Headers.MOBILE_NAME_COOKIE_VALUE)
                .queryParam(SparebankenVestConstants.QueryParams.SO_KEY, securityToken)
                .queryParam(
                        SparebankenVestConstants.QueryParams.IS_NEW_ACTIVATION_KEY,
                        SparebankenVestConstants.QueryParams.IS_NEW_ACTIVATION_VALUE)
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
                .header(
                        SparebankenVestConstants.Headers.ACCEPT,
                        SparebankenVestConstants.Headers.TEXT_HTML_APPLICATION_XHTML_XML)
                .cookie(
                        SparebankenVestConstants.Headers.MOBILE_NAME_COOKIE_KEY,
                        SparebankenVestConstants.Headers.MOBILE_NAME_COOKIE_VALUE)
                .header(
                        SparebankenVestConstants.Headers.ORIGIN_KEY,
                        SparebankenVestConstants.Urls.HOST_SECURITY)
                .post(String.class, loginRequestBody);
    }

    public String postSecurityParamsAuthentication(SecurityParamsRequestBody loginRequestBody) {
        return getPostSecurityParamsRequest()
                .header(
                        SparebankenVestConstants.Headers.ACCEPT,
                        SparebankenVestConstants.Headers.TEXT_HTML_APPLICATION_XHTML_XML)
                .cookie(
                        SparebankenVestConstants.Headers.MOBILE_NAME_COOKIE_KEY,
                        SparebankenVestConstants.Headers.MOBILE_NAME_COOKIE_VALUE)
                .header(
                        SparebankenVestConstants.Headers.ORIGIN_KEY,
                        SparebankenVestConstants.Urls.HOST_SECURITY)
                .post(String.class, loginRequestBody);
    }

    public void finalizeLogin(SecurityParamsRequestBody loginRequestBody) {
        getRequest(SparebankenVestConstants.Urls.DBANK)
                .header(
                        SparebankenVestConstants.Headers.ORIGIN_KEY,
                        SparebankenVestConstants.Urls.HOST_SECURITY)
                .post(HttpResponse.class, loginRequestBody);
    }

    public AccountListResponse fetchAccounts() {
        return getRequest(SparebankenVestConstants.Urls.ACCOUNTS).get(AccountListResponse.class);
    }

    public TransactionsListResponse fetchTransactions(String accountId, String from, String to) {
        TransactionsRequestBody transactionsReqBody =
                new TransactionsRequestBody.Builder(accountId)
                        .withDirection("Alle")
                        .fromDate(from)
                        .toDate(to)
                        .build();

        return this.client
                .request(SparebankenVestConstants.Urls.TRANSACTIONS)
                .header(
                        SparebankenVestConstants.Headers.ACCEPT,
                        SparebankenVestConstants.Headers.APPLICATION_JSON)
                .header(
                        SparebankenVestConstants.Headers.CONTENT_TYPE,
                        SparebankenVestConstants.Headers.APPLICATION_JSON)
                .header(
                        SparebankenVestConstants.Headers.ORIGIN_KEY,
                        SparebankenVestConstants.Urls.HOST_SECURITY)
                .post(TransactionsListResponse.class, transactionsReqBody);
    }

    public DuePaymentsResponse fetchUpcomingTransactions() {
        return getRequest(SparebankenVestConstants.Urls.DUE_PAYMENTS)
                .header(SparebankenVestConstants.Headers.XCSRF_TOKEN, getCsrfTokenCookieValue())
                .get(DuePaymentsResponse.class);
    }

    public FetchLoansResponse fetchLoans() {
        return getRequest(SparebankenVestConstants.Urls.LOANS).get(FetchLoansResponse.class);
    }

    public String innloggetReq(InnloggetRequest innloggetRequest) {
        return client.request(SparebankenVestConstants.Urls.LOGIN_INLOGGET)
                .header(
                        SparebankenVestConstants.Headers.ORIGIN_KEY,
                        SparebankenVestConstants.Urls.HOST_SECURITY)
                .type(MediaType.APPLICATION_FORM_URLENCODED)
                .post(String.class, innloggetRequest);
    }

    public FetchCreditCardsResponse fetchCreditCardAccounts() {
        getRequest(SparebankenVestConstants.Urls.LOGIN_INLOGGET_KORT).get(HttpResponse.class);

        String htmlString = getInnloggningKort();
        Document doc = Jsoup.parse(htmlString);

        Element wresultElement =
                doc.getElementsByAttributeValue(
                                SparebankenVestConstants.SecurityParameters.NAME,
                                SparebankenVestConstants.SecurityParameters.WRESULT)
                        .first();

        Preconditions.checkState(wresultElement != null, "Could not parse wresult from response.");

        String wresult =
                wresultElement
                        .getElementsByAttribute(SparebankenVestConstants.HttpElements.VALUE)
                        .attr(SparebankenVestConstants.HttpElements.VALUE);

        InnloggetRequest innloggetRequest = InnloggetRequest.build(wresult);
        innloggetReq(innloggetRequest);

        getRequest(SparebankenVestConstants.Urls.LOGIN_INLOGGET_KORT).get(HttpResponse.class);

        return this.client
                .request(SparebankenVestConstants.Urls.CREDIT_CARD_ACCOUNTS)
                .header(
                        SparebankenVestConstants.Headers.ACCEPT,
                        SparebankenVestConstants.Headers.APPLICATION_FORM_URL_ENCODED)
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
            ApiIdentifier apiIdentifier, Date fromDate, Date toDate, int startIndex) {
        int endIndex =
                startIndex + SparebankenVestConstants.PagePagination.MAX_TRANSACTIONS_IN_BATCH - 1;

        return getRequest(SparebankenVestConstants.Urls.CREDIT_CARD_TRANSACTIONS)
                .queryParam(
                        SparebankenVestConstants.QueryParams.CARD_NUMBER_GUID_KEY,
                        apiIdentifier.getCardNumberGuid())
                .queryParam(
                        SparebankenVestConstants.QueryParams.KID_GUID_KEY,
                        apiIdentifier.getKidGuid())
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

    private String getInnloggningKort() {
        return this.client
                .request(SparebankenVestConstants.Urls.STS_PRIVATE_WEB)
                .queryParam(
                        SparebankenVestConstants.QueryParams.WA,
                        SparebankenVestConstants.QueryParams.WA_VALUE)
                .queryParam(
                        SparebankenVestConstants.QueryParams.WTREALM,
                        SparebankenVestConstants.QueryParams.WTREALM_VALUE)
                .queryParam(
                        SparebankenVestConstants.QueryParams.WCTX,
                        SparebankenVestConstants.QueryParams.WCTX_VALUE)
                .queryParam(SparebankenVestConstants.QueryParams.WCT, DateUtil.now().toString())
                .get(String.class);
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
