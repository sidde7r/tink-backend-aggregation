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
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.Sparebank1Constants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.Sparebank1Constants.Headers;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.Sparebank1Constants.Keys;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.Sparebank1Constants.Parameters;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.Sparebank1Constants.QueryParams;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.Sparebank1Constants.Urls;
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
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.entities.MessageEntity;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.fetcher.entities.LoanDetailsEntity;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.fetcher.rpc.CreditCardTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.fetcher.rpc.TransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.rpc.ErrorMessageResponse;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.rpc.FinancialInstituationsListResponse;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;

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

    public <T> T getAccounts(URL url, Class<T> responseClass) {
        return client.request(url.parameter(Parameters.BANK_NAME, bankName)).get(responseClass);
    }

    public FinancialInstitutionEntity getFinancialInstitution() {
        Optional<FinancialInstitutionEntity> finInstitution =
                client.request(Urls.CMS).get(FinancialInstituationsListResponse.class)
                        .getFinancialInstitutions().stream()
                        .filter(fe -> Objects.equal(fe.getId(), bankId))
                        .findFirst();

        if (!finInstitution.isPresent()) {
            throw new IllegalStateException(
                    String.format("Bank (%s) not present in list of banks", bankId));
        }

        financialInstitution = finInstitution.get();

        return finInstitution.get();
    }

    public void initActivation() {
        LinkEntity activationLinkEntity =
                Preconditions.checkNotNull(
                        financialInstitution.getLinks().get(Keys.ACTIVATION_KEY),
                        "Activation link not found");

        get(activationLinkEntity.getHref(), HttpResponse.class);
    }

    public String getLoginDispatcher() {
        return client.request(Urls.LOGIN_DISPATCHER)
                .queryParam(QueryParams.APP, QueryParams.APP_VALUE)
                .queryParam(QueryParams.FIN_INST, bankId)
                .queryParam(QueryParams.GOTO, Urls.CONTINUE_ACTIVATION.toString())
                .get(String.class);
    }

    public HttpResponse postLoginInformation(String loginDispatcherHtmlString, String nationalId) {
        Document loginDoc = Jsoup.parse(loginDispatcherHtmlString);
        Element viewState =
                Preconditions.checkNotNull(
                        loginDoc.getElementById("j_id1:javax.faces.ViewState:0"),
                        "viewState element not found, it's needed for the init login request body");

        InitLoginBody initLoginBody = new InitLoginBody(nationalId, viewState.val());

        return client.request(Urls.LOGIN_DISPATCHER)
                .header(Headers.ORIGIN, Urls.BASE_LOGIN)
                .type(MediaType.APPLICATION_FORM_URLENCODED)
                .queryParam(Parameters.CID, Parameters.CID_VALUE)
                .post(HttpResponse.class, initLoginBody);
    }

    public String selectMarketAndAuthentication() {
        return client.request(Urls.SELECT_MARKET_AND_AUTH_TYPE)
                .queryParam(QueryParams.APP, QueryParams.APP_VALUE)
                .queryParam(QueryParams.FIN_INST, bankId)
                .queryParam(QueryParams.MARKET, QueryParams.MARKET_VALUE)
                .queryParam(QueryParams.GOTO, QueryParams.CONTINUE_ACTIVATION_URL)
                .get(String.class);
    }

    public String initBankId(
            String selectMarketAndAuthenticationHtmlString, String mobilenumber, String dob) {
        InitBankIdBody initBankIdBody =
                getInitBankIdBody(selectMarketAndAuthenticationHtmlString, mobilenumber, dob);

        return client.request(Urls.SELECT_MARKET_AND_AUTH_TYPE)
                .header(Headers.ORIGIN, Urls.BASE_LOGIN)
                .type(MediaType.APPLICATION_FORM_URLENCODED)
                .queryParam(Parameters.CID, Parameters.CID_VALUE)
                .post(String.class, initBankIdBody);
    }

    private InitBankIdBody getInitBankIdBody(String htmlResponse, String mobilenumber, String dob) {
        Document initBankIdDoc = Jsoup.parse(htmlResponse);

        Element form =
                Preconditions.checkNotNull(
                        initBankIdDoc.getElementById("panel-bankID-mobile").select("form").first(),
                        "Could not find bankID panel in html response.");

        String formId = form.id();
        Element viewState =
                Preconditions.checkNotNull(
                        form.getElementById("j_id1:javax.faces.ViewState:1"),
                        "viewState element not found, it's needed for the init bankID request body");

        return new InitBankIdBody(mobilenumber, dob, formId, viewState.val());
    }

    public PollBankIdResponse pollBankId() {
        return client.request(Urls.POLL_BANKID)
                .header(Headers.X_REQUESTED_WITH, Headers.XML_HTTP_REQUEST)
                .header(Headers.ORIGIN, Urls.BASE_LOGIN)
                .type(MediaType.APPLICATION_JSON)
                .queryParam(Parameters.CID, Parameters.CID_VALUE)
                .post(PollBankIdResponse.class);
    }

    public void loginDone() {
        client.request(Urls.LOGIN_DONE)
                .queryParam(Parameters.CID, Parameters.CID_VALUE)
                .get(HttpResponse.class);
    }

    public HttpResponse continueActivation() {
        return client.request(Urls.CONTINUE_ACTIVATION)
                .accept(Headers.TEXT_HTML_APPLICATION_XHTML_XML)
                .get(HttpResponse.class);
    }

    public AgreementsResponse getAgreement() {
        return client.request(Urls.AGREEMENTS.parameter(Parameters.BANK_NAME, bankName))
                .accept(MediaType.WILDCARD)
                .header(Headers.X_REQUESTED_WITH, Headers.XML_HTTP_REQUEST)
                .get(AgreementsResponse.class);
    }

    public void finishAgreementSession(AgreementsResponse agreementsResponse) {
        TargetUrlRequest targetUrlRequest = new TargetUrlRequest();
        targetUrlRequest.setAgreementId(agreementsResponse.getAgreements().get(0).getAgreementId());

        // Find the cookie DSESSIONID cookie, need to set the X-CSRFToken header to the value of
        // this cookie.
        Cookie dSessionIdCookie = getDSessionIdCookie();

        TargetUrlResponse response =
                client.request(Urls.AGREEMENTS.parameter(Parameters.BANK_NAME, bankName))
                        .header(Headers.ORIGIN, Urls.BASE)
                        .header(Headers.X_REQUESTED_WITH, Headers.XML_HTTP_REQUEST)
                        .header(
                                Headers.REFERER,
                                Urls.BASE + bankName + Headers.REFERER_FOR_FINISH_AGREEMENT_SESSION)
                        .header(Headers.CSRFT_TOKEN, dSessionIdCookie.getValue())
                        .accept(MediaType.WILDCARD)
                        .type(MediaType.APPLICATION_JSON)
                        .put(TargetUrlResponse.class, targetUrlRequest);

        Preconditions.checkState(
                !Strings.isNullOrEmpty(response.getTargetUrl()), "Did not receive target url");

        get(response.getTargetUrl(), HttpResponse.class);
    }

    public FinishActivationResponse finishActivation(Sparebank1Identity identity, String url) {
        FinishActivationRequest request = FinishActivationRequest.create(identity);

        return getActiveSessionRequest(url).post(FinishActivationResponse.class, request);
    }

    public InitiateAuthenticationResponse initAuthentication(
            Sparebank1Identity identity, String url) throws BankServiceException {
        InitiateAuthenticationRequest request = InitiateAuthenticationRequest.create(identity);

        try {
            return client.request(url)
                    .header(Headers.X_SB1_REST_VERSION, Headers.X_SB1_REST_VERSION_VALUE)
                    .accept(Headers.APPLICATION_JSON_CHARSET_UTF8)
                    .type(MediaType.APPLICATION_JSON)
                    .post(InitiateAuthenticationResponse.class, request);
        } catch (HttpResponseException e) {

            ErrorMessageResponse errorResponse =
                    e.getResponse().getBody(ErrorMessageResponse.class);
            Optional<MessageEntity> serviceUnavailableMessage =
                    errorResponse.getMessages().stream()
                            .filter(this::serviceUnavailable)
                            .findFirst();

            if (serviceUnavailableMessage.isPresent()) {
                throw BankServiceError.NO_BANK_SERVICE.exception(e);
            }

            throw e;
        }
    }

    public FinishAuthenticationResponse finishAuthentication(
            String url, FinishAuthenticationRequest request) throws SessionException {
        try {
            return getActiveSessionRequest(url).put(FinishAuthenticationResponse.class, request);
        } catch (HttpResponseException e) {

            ErrorMessageResponse errorResponse =
                    e.getResponse().getBody(ErrorMessageResponse.class);
            Optional<MessageEntity> badCredentialsMessage =
                    errorResponse.getMessages().stream()
                            .filter(this::userDeletedTinkProfileAtBank)
                            .findFirst();

            if (badCredentialsMessage.isPresent()) {
                throw SessionError.SESSION_EXPIRED.exception(e);
            }

            throw e;
        }
    }

    private boolean serviceUnavailable(MessageEntity errorMessage) {
        return ErrorMessages.SERVICE_UNAVAILABLE.equalsIgnoreCase(errorMessage.getKey());
    }

    private boolean userDeletedTinkProfileAtBank(MessageEntity errorMessage) {
        return ErrorMessages.SRP_BAD_CREDENTIALS.equalsIgnoreCase(errorMessage.getKey());
    }

    public CreditCardTransactionsResponse fetchCreditCardTransactions(String bankIdentifier) {
        return client.request(
                        Urls.CREDITCARD_TRANSACTIONS
                                .parameter(Parameters.BANK_NAME, bankName)
                                .parameter(Parameters.ACCOUNT_ID, bankIdentifier))
                .get(CreditCardTransactionsResponse.class);
    }

    public LoanDetailsEntity fetchLoanDetails(String loanId) {
        return client.request(
                        Urls.LOAN_DETAILS
                                .parameter(Parameters.BANK_NAME, bankName)
                                .parameter(Parameters.ACCOUNT_ID, loanId))
                .queryParam(QueryParams.UNDERSCORE, String.valueOf(System.currentTimeMillis()))
                .get(LoanDetailsEntity.class);
    }

    public TransactionsResponse fetchTransactions(String url) {
        return client.request(url).get(TransactionsResponse.class);
    }

    public HttpResponse logout(String url) {
        return getActiveSessionRequest(url).delete(HttpResponse.class);
    }

    private RequestBuilder getActiveSessionRequest(String url) {
        // Find the cookie DSESSIONID cookie, need to set the X-CSRFToken header to the value of
        // this cookie.
        Cookie dSessionIdCookie = getDSessionIdCookie();

        return client.request(url)
                .header(Headers.CSRFT_TOKEN, dSessionIdCookie.getValue())
                .header(Headers.X_SB1_REST_VERSION, Headers.X_SB1_REST_VERSION_VALUE)
                .accept(Headers.APPLICATION_JSON_CHARSET_UTF8)
                .type(MediaType.APPLICATION_JSON);
    }

    private Cookie getDSessionIdCookie() {
        Optional<Cookie> dSessionIdCookie =
                client.getCookies().stream()
                        .filter(cookie -> Keys.SESSION_ID.equalsIgnoreCase(cookie.getName()))
                        .findFirst();

        if (!dSessionIdCookie.isPresent()) {
            throw new IllegalStateException("DSESSIONID cookie is not present");
        }

        return dSessionIdCookie.get();
    }
}
