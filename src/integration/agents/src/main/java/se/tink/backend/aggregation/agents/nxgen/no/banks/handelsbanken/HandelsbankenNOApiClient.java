package se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken;

import com.google.common.base.Preconditions;
import java.util.List;
import javax.ws.rs.core.MediaType;
import org.apache.http.HttpStatus;
import org.apache.http.cookie.Cookie;
import se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.HandelsbankenNOConstants.Headers;
import se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.HandelsbankenNOConstants.Tags;
import se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.HandelsbankenNOConstants.Url;
import se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.authenticator.rpc.FinalizeBankIdRequest;
import se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.authenticator.rpc.FirstLoginRequest;
import se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.authenticator.rpc.FirstLoginResponse;
import se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.authenticator.rpc.InitBankIdRequest;
import se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.authenticator.rpc.PollBankIdResponse;
import se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.authenticator.rpc.SecondLoginResponse;
import se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.authenticator.rpc.SendSmsRequest;
import se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.authenticator.rpc.VerifyCustomerResponse;
import se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.fetcher.investment.rpc.AksjerOverviewResponse;
import se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.fetcher.investment.rpc.AvailableBalanceResponse;
import se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.fetcher.investment.rpc.FinalizeAksjerLoginRequest;
import se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.fetcher.investment.rpc.FinalizeInvestorLoginRequest;
import se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.fetcher.investment.rpc.InitInvestmentsLoginResponse;
import se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.fetcher.investment.rpc.InvestmentsOverviewResponse;
import se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.fetcher.investment.rpc.PositionsListEntity;
import se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.fetcher.loan.rpc.LoanDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.fetcher.loan.rpc.LoanFetchingResponse;
import se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.fetcher.transactionalaccount.rpc.AccountFetchingResponse;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.date.DateUtils;
import se.tink.libraries.date.ThreadSafeDateFormat;

public class HandelsbankenNOApiClient {
    private final TinkHttpClient client;
    private final SessionStorage sessionStorage;

    private List<AccountEntity> accountList;

    public HandelsbankenNOApiClient(TinkHttpClient client, SessionStorage sessionStorage) {
        this.client = client;
        this.sessionStorage = sessionStorage;
    }

    private RequestBuilder requestWithUserAgent(URL url) {
        return client.request(url).header(Headers.USER_AGENT);
    }

    private RequestBuilder requestInSession(URL url) {
        return requestWithUserAgent(url)
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON)
                .header(Headers.X_EVRY_CLIENT_REQUESTID)
                .header(Headers.X_EVRY_CLIENT)
                .header(
                        HandelsbankenNOConstants.Header.EVRY_TOKEN,
                        sessionStorage.get(Tags.ACCESS_TOKEN))
                .cookie(
                        sessionStorage.get(Tags.SESSION_STAMP),
                        sessionStorage.get(Tags.SESSION_STAMP_VALUE))
                .cookie(Tags.NONCE, sessionStorage.get(Tags.NONCE));
    }

    // === Authentication API calls ===
    public void fetchAppInformation() {
        client.request(Url.APP_INFORMATION.get())
                .accept(MediaType.WILDCARD)
                .get(HttpResponse.class);
    }

    public VerifyCustomerResponse verifyCustomer(String nationalId, String mobileNumber) {
        return client.request(Url.VERIFY_CUSTOMER.parameters(nationalId, mobileNumber))
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .type(MediaType.APPLICATION_JSON)
                .header(Headers.X_EVRY_CLIENT_REQUESTID)
                .header(Headers.X_EVRY_CLIENT)
                .get(VerifyCustomerResponse.class);
    }

    public void configureBankId(String nationalId, String mobileNumber) {
        client.request(Url.CONFIGURE_BANKID.parameters(nationalId, mobileNumber))
                .accept(MediaType.TEXT_HTML_TYPE)
                .accept(MediaType.APPLICATION_XHTML_XML_TYPE)
                .accept(MediaType.APPLICATION_XML_TYPE)
                .get(HttpResponse.class);

        String jSession =
                client.getCookies().stream()
                        .filter(cookie -> cookie.getName().equalsIgnoreCase(Tags.JSESSION_ID))
                        .findFirst()
                        .map(Cookie::getValue)
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "JSessionID is not found during authentication"));

        sessionStorage.put(Tags.JSESSION_ID, jSession);
    }

    public String initBankId(InitBankIdRequest initBankIdRequest) {
        URL url = Url.BANKID_1.parameters(sessionStorage.get(Tags.JSESSION_ID));

        return client.request(url)
                .header(Headers.ORIGIN)
                .type(MediaType.APPLICATION_FORM_URLENCODED)
                .post(String.class, initBankIdRequest);
    }

    public PollBankIdResponse pollBankId() {
        URL url = Url.POLL_BANK.parameters(sessionStorage.get(Tags.JSESSION_ID));

        return client.request(url)
                .accept(MediaType.WILDCARD)
                .header(Headers.ORIGIN)
                .header(Headers.REQUEST_WITH)
                .post(PollBankIdResponse.class);
    }

    public String finalizeBankId(FinalizeBankIdRequest finalizeBankIdRequest) {
        URL url = Url.BANKID_2.parameters(sessionStorage.get(Tags.JSESSION_ID));

        return client.request(url)
                .header(Headers.ORIGIN)
                .type(MediaType.APPLICATION_FORM_URLENCODED)
                .post(String.class, finalizeBankIdRequest);
    }

    public FirstLoginResponse loginFirstStep(FirstLoginRequest firstLoginRequest) {
        FirstLoginResponse firstLoginResponse =
                client.request(Url.LOGIN_FIRST_STEP.get())
                        .accept(MediaType.APPLICATION_JSON)
                        .type(MediaType.APPLICATION_JSON)
                        .header(Headers.USER_AGENT)
                        .header(Headers.X_EVRY_CLIENT)
                        .post(FirstLoginResponse.class, firstLoginRequest);

        List<Cookie> cookies = client.getCookies();

        String nonce =
                cookies.stream()
                        .filter(cookie -> cookie.getName().equalsIgnoreCase(Tags.NONCE))
                        .findFirst()
                        .map(Cookie::getValue)
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "SECESB_NONCE is not found during authentication"));

        Cookie sessionStampCookie =
                cookies.stream()
                        .filter(
                                cookie ->
                                        cookie.getName()
                                                .contains(
                                                        HandelsbankenNOConstants.Tags
                                                                .SESSION_STAMP))
                        .findFirst()
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "SESSSION_STAMP is not found during authentication"));

        sessionStorage.put(HandelsbankenNOConstants.Tags.NONCE, nonce);
        sessionStorage.put(
                HandelsbankenNOConstants.Tags.SESSION_STAMP, sessionStampCookie.getName());
        sessionStorage.put(
                HandelsbankenNOConstants.Tags.SESSION_STAMP_VALUE, sessionStampCookie.getValue());
        return firstLoginResponse;
    }

    public SecondLoginResponse loginSecondStep() {
        return client.request(Url.LOGIN_SECOND_STEP.get())
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON)
                .header(Headers.USER_AGENT)
                .header(Headers.X_EVRY_CLIENT)
                .header(
                        HandelsbankenNOConstants.Header.EVRY_TOKEN,
                        sessionStorage.get(Tags.ACCESS_TOKEN))
                .post(SecondLoginResponse.class)
                .throwErrorIfAgreementIsInactive();
    }

    public HttpResponse sendSms(SendSmsRequest sendSmsRequest) {
        return requestInSession(Url.SEND_SMS.get()).post(HttpResponse.class, sendSmsRequest);
    }

    public List<AccountEntity> fetchAccounts() {
        if (accountList != null && !accountList.isEmpty()) {
            return accountList;
        }

        AccountFetchingResponse accountsResponse =
                requestInSession(Url.ACCOUNTS.get()).get(AccountFetchingResponse.class);

        List<AccountEntity> accounts = accountsResponse.getAccounts();
        this.accountList = accounts;

        return accounts;
    }

    public HttpResponse fetchTransactions(String uri, int number, int index) {
        URL url = Url.TRANSACTIONS.parameters(uri, String.valueOf(number), String.valueOf(index));
        return requestInSession(url).get(HttpResponse.class);
    }

    public LoanFetchingResponse fetchLoans() {
        return requestInSession(Url.LOANS.get()).get(LoanFetchingResponse.class);
    }

    public LoanDetailsResponse fetchLoanDetails(String repaymentPlanForLoanAccountPath) {
        return requestInSession(Url.LOAN_DETAILS.parameters(repaymentPlanForLoanAccountPath))
                .get(LoanDetailsResponse.class);
    }

    HttpResponse fetchUserSettings() {
        return requestInSession(Url.USER_SETTINGS.get()).get(HttpResponse.class);
    }

    public InitInvestmentsLoginResponse initInvestmentLogin() {
        return requestInSession(
                        Url.INIT_INVESTOR_LOGIN.queryParam(
                                HandelsbankenNOConstants.QueryParamPairs.SHIBBOLETH_ENDPOINT
                                        .getKey(),
                                HandelsbankenNOConstants.QueryParamPairs.SHIBBOLETH_ENDPOINT
                                        .getValue()))
                .post(InitInvestmentsLoginResponse.class);
    }

    public String investorCustomerPortalLogin(String so) {
        URL url =
                HandelsbankenNOConstants.Url.CUSTOMER_PORTAL_LOGIN
                        .get()
                        .queryParam(HandelsbankenNOConstants.QueryParams.SO, so)
                        .queryParam(
                                HandelsbankenNOConstants.QueryParamPairs.INVESTOR_PROVIDER_ID
                                        .getKey(),
                                HandelsbankenNOConstants.QueryParamPairs.INVESTOR_PROVIDER_ID
                                        .getValue())
                        .queryParam(
                                HandelsbankenNOConstants.QueryParamPairs.INVESTOR_TARGET.getKey(),
                                HandelsbankenNOConstants.QueryParamPairs.INVESTOR_TARGET
                                        .getValue());

        return requestInSession(url).get(String.class);
    }

    public String aksjerCustomerPortalLogin(String so) {
        URL url =
                HandelsbankenNOConstants.Url.CUSTOMER_PORTAL_LOGIN
                        .get()
                        .queryParam(HandelsbankenNOConstants.QueryParams.SO, so)
                        .queryParam(
                                HandelsbankenNOConstants.QueryParamPairs.AKSJER_PROVIDER_ID
                                        .getKey(),
                                HandelsbankenNOConstants.QueryParamPairs.AKSJER_PROVIDER_ID
                                        .getValue());

        return requestInSession(url).get(String.class);
    }

    public void finalizeInvestorLogin(String samlResponse) {
        HttpResponse response =
                requestWithUserAgent(HandelsbankenNOConstants.Url.INVESTOR_LOGIN.get())
                        .type(MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                        .post(HttpResponse.class, new FinalizeInvestorLoginRequest(samlResponse));

        Preconditions.checkState(
                response.getStatus() == HttpStatus.SC_OK,
                "Login to investor unsuccessful, could not fetch investment accounts.");
    }

    public void finalizeAksjerLogin(String samlResponse) {
        HttpResponse response =
                requestWithUserAgent(Url.AKSJER_LOGIN.get())
                        .type(MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                        .post(HttpResponse.class, new FinalizeAksjerLoginRequest(samlResponse));

        Preconditions.checkState(
                response.getStatus() == HttpStatus.SC_OK,
                "Login to stock portal unsuccessful, could not fetch investment accounts.");
    }

    public InvestmentsOverviewResponse fetchInvestmentsOverview(String username) {
        return requestWithUserAgent(
                        HandelsbankenNOConstants.Url.INVESTMENTS_OVERVIEW
                                .get()
                                .parameter(HandelsbankenNOConstants.UrlParameters.DOB, username))
                .get(InvestmentsOverviewResponse.class);
    }

    public AksjerOverviewResponse getAksjerOverview() {
        return requestWithUserAgent(HandelsbankenNOConstants.Url.AKSJER_OVERVIEW.get())
                .get(AksjerOverviewResponse.class);
    }

    public AvailableBalanceResponse getAksjerAvailableBalance(String username, String customerId) {
        return requestWithUserAgent(
                        HandelsbankenNOConstants.Url.AKSJER_AVAILABLE_BALANCE
                                .get()
                                .parameter(HandelsbankenNOConstants.UrlParameters.DOB, username)
                                .parameter(
                                        HandelsbankenNOConstants.UrlParameters.CUSTOMER_ID,
                                        customerId))
                .get(AvailableBalanceResponse.class);
    }

    public PositionsListEntity getPositions(String csdAccountNumber) {
        return requestWithUserAgent(
                        HandelsbankenNOConstants.Url.POSITIONS
                                .get()
                                .parameter(
                                        HandelsbankenNOConstants.UrlParameters.ACCOUNT_NUMBER,
                                        csdAccountNumber))
                .queryParam(
                        HandelsbankenNOConstants.QueryParams.DATE,
                        ThreadSafeDateFormat.FORMATTER_DAILY.format(DateUtils.getToday()))
                .get(PositionsListEntity.class);
    }
}
