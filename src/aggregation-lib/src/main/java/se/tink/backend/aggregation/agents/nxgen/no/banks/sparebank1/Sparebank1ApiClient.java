package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import java.util.Optional;
import javax.ws.rs.core.MediaType;
import org.apache.http.cookie.Cookie;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.authenticator.rpc.authentication.FinishAuthenticationRequest;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.authenticator.rpc.authentication.FinishAuthenticationResponse;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.authenticator.rpc.authentication.InitiateAuthenticationRequest;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.authenticator.rpc.authentication.InitiateAuthenticationResponse;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.authenticator.rpc.useractivation.AgreementsResponse;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.authenticator.rpc.useractivation.FinishActivationRequest;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.authenticator.rpc.useractivation.FinishActivationResponse;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.authenticator.rpc.useractivation.InitBankIdBody;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.authenticator.rpc.useractivation.InitLoginBody;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.authenticator.rpc.useractivation.PollBankIdResponse;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.authenticator.rpc.useractivation.TargetUrlRequest;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.authenticator.rpc.useractivation.TargetUrlResponse;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.entities.FinancialInstitutionEntity;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.entities.LinkEntity;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.fetcher.entities.LoanDetailsEntity;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.fetcher.rpc.AccountListResponse;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.fetcher.rpc.CreditCardAccountsListResponse;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.fetcher.rpc.CreditCardTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.fetcher.rpc.LoanListResponse;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.fetcher.rpc.PortfolioEntitiesResponse;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.fetcher.rpc.TransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.rpc.FinancialInstituationsListResponse;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;

public class Sparebank1ApiClient {
    private final String bankId;
    private final String bankName;
    private final TinkHttpClient client;

    private FinancialInstitutionEntity financialInstitution;

    public Sparebank1ApiClient(TinkHttpClient client, String bankId) {
        this.client = client;
        this.bankId = bankId;
        // Remove "fid-" prefix from the bankId
        this.bankName = bankId.substring(4);
    }

    public <T> T get(String url, Class<T> responseClass) {
        return get(new URL(url), responseClass);
    }

    public <T> T get(URL url, Class<T> responseClass) {
        return client.request(url).get(responseClass);
    }

    public FinancialInstitutionEntity getFinancialInstitution() {
        Optional<FinancialInstitutionEntity> finInstitution = client.request(Sparebank1Constants.Urls.CMS)
                .get(FinancialInstituationsListResponse.class).getFinancialInstitutions()
                .stream()
                .filter(fe -> Objects.equal(fe.getId(), bankId))
                .findFirst();

        if (!finInstitution.isPresent()) {
            throw new IllegalStateException(String.format("Bank (%s) not present in list of banks", bankId));
        }

        financialInstitution = finInstitution.get();

        return finInstitution.get();
    }

    public void initActivation() {
        LinkEntity activationLinkEntity = Preconditions.checkNotNull(
                financialInstitution.getLinks().get(Sparebank1Constants.Keys.ACTIVATION_KEY),
                "Activation link not found");

        client.request(activationLinkEntity.getHref()).get(HttpResponse.class);
    }

    public String getLoginDispatcher() {
        return client.request(Sparebank1Constants.Urls.GET_LOGIN_DISPATCHER)
                .queryParam(Sparebank1Constants.QueryParams.APP,
                        Sparebank1Constants.QueryParams.APP_VALUE)
                .queryParam(Sparebank1Constants.QueryParams.FIN_INST, bankId)
                .queryParam(Sparebank1Constants.QueryParams.GOTO,
                        Sparebank1Constants.Urls.CONTINUE_ACTIVATION.toString())
                .get(String.class);
    }

    public HttpResponse postLoginInformation(String loginDispatcherHtmlString, String nationalId) {
        Document loginDoc = Jsoup.parse(loginDispatcherHtmlString);
        Element viewState = Preconditions.checkNotNull(loginDoc.getElementById("j_id1:javax.faces.ViewState:0"),
                "viewState element not found, it's needed for the init login request body");

        InitLoginBody initLoginBody = new InitLoginBody(nationalId, viewState.val());

        return client.request(Sparebank1Constants.Urls.INIT_LOGIN)
                .header(Sparebank1Constants.Headers.ORIGIN, Sparebank1Constants.Urls.BASE_LOGIN)
                .type(MediaType.APPLICATION_FORM_URLENCODED)
                .queryParam(Sparebank1Constants.Parameters.CID,
                        Sparebank1Constants.Parameters.CID_VALUE)
                .post(HttpResponse.class, initLoginBody);
    }

    public String selectMarketAndAuthentication() {
        return client.request(Sparebank1Constants.Urls.SELECT_MARKET_AND_AUTH_TYPE)
                .queryParam(Sparebank1Constants.QueryParams.APP,
                        Sparebank1Constants.QueryParams.APP_VALUE)
                .queryParam(Sparebank1Constants.QueryParams.FIN_INST, bankId)
                .queryParam(Sparebank1Constants.QueryParams.MARKET,
                        Sparebank1Constants.QueryParams.MARKET_VALUE)
                .queryParam(Sparebank1Constants.QueryParams.GOTO,
                        Sparebank1Constants.QueryParams.CONTINUE_ACTIVATION_URL)
                .get(String.class);
    }


    public String initBankId(String selectMarketAndAuthenticationHtmlString, String mobilenumber, String dob) {
        InitBankIdBody initBankIdBody = getInitBankIdBody(
                selectMarketAndAuthenticationHtmlString, mobilenumber, dob);

        return client.request(Sparebank1Constants.Urls.SELECT_MARKET_AND_AUTH_TYPE)
                .header(Sparebank1Constants.Headers.ORIGIN, Sparebank1Constants.Urls.BASE_LOGIN)
                .type(MediaType.APPLICATION_FORM_URLENCODED)
                .queryParam(Sparebank1Constants.Parameters.CID, Sparebank1Constants.Parameters.CID_VALUE)
                .post(String.class, initBankIdBody);
    }

    private InitBankIdBody getInitBankIdBody(String htmlResponse, String mobilenumber, String dob) {
        Document initBankIdDoc = Jsoup.parse(htmlResponse);

        Element form = Preconditions.checkNotNull(
                initBankIdDoc.getElementById("panel-bankID-mobile").select("form").first(),
                "Could not find bankID panel in html response.");

        String formId = form.id();
        Element viewState = Preconditions.checkNotNull(
                form.getElementById("j_id1:javax.faces.ViewState:1"),
                "viewState element not found, it's needed for the init bankID request body");

        return new InitBankIdBody(mobilenumber, dob, formId, viewState.val());
    }

    public PollBankIdResponse pollBankId() {
        return client.request(Sparebank1Constants.Urls.POLL_BANKID)
                .header(Sparebank1Constants.Headers.X_REQUESTED_WITH,
                        Sparebank1Constants.Headers.XML_HTTP_REQUEST)
                .header(Sparebank1Constants.Headers.ORIGIN,
                        Sparebank1Constants.Urls.BASE_LOGIN)
                .type(MediaType.APPLICATION_JSON)
                .queryParam(Sparebank1Constants.Parameters.CID, Sparebank1Constants.Parameters.CID_VALUE)
                .post(PollBankIdResponse.class);
    }

    public void loginDone() {
        client.request(Sparebank1Constants.Urls.LOGIN_DONE)
                .queryParam(Sparebank1Constants.Parameters.CID, Sparebank1Constants.Parameters.CID_VALUE)
                .get(HttpResponse.class);
    }

    public HttpResponse continueActivation() {
        return client.request(Sparebank1Constants.Urls.CONTINUE_ACTIVATION)
                .accept(Sparebank1Constants.Headers.TEXT_HTML_APPLICATION_XHTML_XML)
                .get(HttpResponse.class);
    }

    public AgreementsResponse getAgreement() {
        return client.request(Sparebank1Constants.Urls.AGREEMENTS
                .parameter(Sparebank1Constants.Parameters.BANK_NAME, bankName))
                .accept(MediaType.WILDCARD)
                .header(Sparebank1Constants.Headers.X_REQUESTED_WITH,
                        Sparebank1Constants.Headers.XML_HTTP_REQUEST)
                .get(AgreementsResponse.class);
    }

    public void finishAgreementSession(AgreementsResponse agreementsResponse) {
        TargetUrlRequest targetUrlRequest = new TargetUrlRequest();
        targetUrlRequest.setAgreementId(agreementsResponse.getAgreements().get(0).getAgreementId());

        // Find the cookie DSESSIONID cookie, need to set the X-CSRFToken header to the value of this cookie.
        Cookie dSessionIdCookie = getDSessionIdCookie();

        TargetUrlResponse response = client.request(Sparebank1Constants.Urls.AGREEMENTS
                .parameter(Sparebank1Constants.Parameters.BANK_NAME, bankName))
                .header(Sparebank1Constants.Headers.ORIGIN, Sparebank1Constants.Urls.BASE)
                .header(Sparebank1Constants.Headers.X_REQUESTED_WITH,
                        Sparebank1Constants.Headers.XML_HTTP_REQUEST)
                .header(Sparebank1Constants.Headers.REFERER, Sparebank1Constants.Urls.BASE + bankName +
                        Sparebank1Constants.Headers.REFERER_FOR_FINISH_AGREEMENT_SESSION)
                .header(Sparebank1Constants.Headers.CSRFT_TOKEN, dSessionIdCookie.getValue())
                .accept(MediaType.WILDCARD)
                .type(MediaType.APPLICATION_JSON)
                .put(TargetUrlResponse.class, targetUrlRequest);

        Preconditions.checkState(!Strings.isNullOrEmpty(response.getTargetUrl()),
                "Did not receive target url");

        client.request(new URL(response.getTargetUrl())).get(HttpResponse.class);
    }

    public FinishActivationResponse finishActivation(Sparebank1Identity identity, String url) {
        // Find the cookie DSESSIONID cookie, need to set the X-CSRFToken header to the value of this cookie.
        Cookie dSessionIdCookie = getDSessionIdCookie();

        FinishActivationRequest request = FinishActivationRequest.create(identity);

        return client.request(url)
                .header(Sparebank1Constants.Headers.CSRFT_TOKEN, dSessionIdCookie.getValue())
                .header(Sparebank1Constants.Headers.X_SB1_REST_VERSION,
                        Sparebank1Constants.Headers.X_SB1_REST_VERSION_VALUE)
                .accept(Sparebank1Constants.Headers.APPLICATION_JSON_CHARSET_UTF8)
                .type(MediaType.APPLICATION_JSON)
                .post(FinishActivationResponse.class, request);
    }

    public InitiateAuthenticationResponse initAuthentication(Sparebank1Identity identity, String url) {
        InitiateAuthenticationRequest request = InitiateAuthenticationRequest.create(identity);

        return client.request(url)
                .header(Sparebank1Constants.Headers.X_SB1_REST_VERSION,
                        Sparebank1Constants.Headers.X_SB1_REST_VERSION_VALUE)
                .accept(Sparebank1Constants.Headers.APPLICATION_JSON_CHARSET_UTF8)
                .type(MediaType.APPLICATION_JSON)
                .post(InitiateAuthenticationResponse.class, request);
    }

    public FinishAuthenticationResponse finishAuthentication(String url, FinishAuthenticationRequest request) {
        // Find the cookie DSESSIONID cookie, need to set the X-CSRFToken header to the value of this cookie.
        Cookie dSessionIdCookie = getDSessionIdCookie();

        return client.request(url)
                .header(Sparebank1Constants.Headers.CSRFT_TOKEN, dSessionIdCookie.getValue())
                .header(Sparebank1Constants.Headers.X_SB1_REST_VERSION,
                        Sparebank1Constants.Headers.X_SB1_REST_VERSION_VALUE)
                .accept(Sparebank1Constants.Headers.APPLICATION_JSON_CHARSET_UTF8)
                .type(MediaType.APPLICATION_JSON)
                .put(FinishAuthenticationResponse.class, request);
    }

    public AccountListResponse fetchAccounts() {
        return client.request(Sparebank1Constants.Urls.ACCOUNTS
                .parameter(Sparebank1Constants.Parameters.BANK_NAME, bankName))
                .get(AccountListResponse.class);
    }

    public CreditCardAccountsListResponse fetchCreditCards() {
        return client.request(Sparebank1Constants.Urls.CREDITCARDS
                .parameter(Sparebank1Constants.Parameters.BANK_NAME, bankName))
                .get(CreditCardAccountsListResponse.class);
    }

    public CreditCardTransactionsResponse fetchCreditCardTransactions(String bankIdentifier) {
        return client.request(Sparebank1Constants.Urls.CREDITCARD_TRANSACTIONS
                .parameter(Sparebank1Constants.Parameters.BANK_NAME, bankName)
                .parameter(Sparebank1Constants.Parameters.ACCOUNT_ID, bankIdentifier))
                .get(CreditCardTransactionsResponse.class);
    }

    public PortfolioEntitiesResponse fetchPortfolios() {
        return client.request(Sparebank1Constants.Urls.PORTFOLIOS
                .parameter(Sparebank1Constants.Parameters.BANK_NAME, bankName))
                .get(PortfolioEntitiesResponse.class);
    }

    public LoanListResponse fetchLoans() {
        return client.request(Sparebank1Constants.Urls.LOANS
                .parameter(Sparebank1Constants.Parameters.BANK_NAME, bankName))
                .get(LoanListResponse.class);
    }

    public LoanDetailsEntity fetchLoanDetails(String loanId) {
        return client.request(Sparebank1Constants.Urls.LOAN_DETAILS
                .parameter(Sparebank1Constants.Parameters.BANK_NAME, bankName)
                .parameter(Sparebank1Constants.Parameters.ACCOUNT_ID, loanId))
                .queryParam(Sparebank1Constants.QueryParams.UNDERSCORE,
                        String.valueOf(System.currentTimeMillis()))
                .get(LoanDetailsEntity.class);
    }

    public TransactionsResponse fetchTransactions(String url) {
        return client.request(url).get(TransactionsResponse.class);
    }

    public HttpResponse logout(String url) {
        // Find the cookie DSESSIONID cookie, need to set the X-CSRFToken header to the value of this cookie.
        Cookie dSessionIdCookie = getDSessionIdCookie();

        return client.request(url)
                .header(Sparebank1Constants.Headers.CSRFT_TOKEN, dSessionIdCookie.getValue())
                .header(Sparebank1Constants.Headers.X_SB1_REST_VERSION,
                        Sparebank1Constants.Headers.X_SB1_REST_VERSION_VALUE)
                .accept(Sparebank1Constants.Headers.APPLICATION_JSON_CHARSET_UTF8)
                .type(MediaType.APPLICATION_JSON)
                .delete(HttpResponse.class);
    }

    private Cookie getDSessionIdCookie() {
        Optional<Cookie> dSessionIdCookie = client.getCookies().stream()
                .filter(cookie -> Sparebank1Constants.Keys.SESSION_ID.equalsIgnoreCase(cookie.getName()))
                .findFirst();

        if (!dSessionIdCookie.isPresent()) {
            throw new IllegalStateException("DSESSIONID cookie is not present");
        }

        return dSessionIdCookie.get();
    }
}
