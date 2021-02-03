package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1;

import javax.ws.rs.core.MediaType;
import lombok.Data;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.Sparebank1Constants.Headers;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.Sparebank1Constants.Keys;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.Sparebank1Constants.Parameters;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.Sparebank1Constants.QueryParams;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.Sparebank1Constants.Urls;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.authenticator.rpc.AgreementRequest;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.authenticator.rpc.AgreementsResponse;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.authenticator.rpc.BankBranchResponse;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.authenticator.rpc.BankBranchResponse.Branch;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.authenticator.rpc.InitSessionRequest;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.authenticator.rpc.InitSessionResponse;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.authenticator.rpc.InitTokenRequest;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.authenticator.rpc.PollBankIdResponse;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.authenticator.rpc.SessionRequest;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.authenticator.rpc.SessionResponse;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.authenticator.rpc.TokenExpirationTimeResponse;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.fetcher.entities.LoanDetailsEntity;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.fetcher.rpc.CreditCardTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.fetcher.rpc.TransactionsResponse;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.url.URL;

@Data
public class Sparebank1ApiClient {
    private final String bankId;
    private final TinkHttpClient client;
    private String sessionToken;

    public Sparebank1ApiClient(TinkHttpClient client, String bankId) {
        this.client = client;
        this.bankId = bankId.substring(4);
    }

    public <T> T get(String url, Class<T> responseClass) {
        return get(new URL(url), responseClass);
    }

    public <T> T get(URL url, Class<T> responseClass) {
        return client.request(url).get(responseClass);
    }

    public <T> T getAccounts(URL url, Class<T> responseClass) {
        return client.request(url.parameter(Parameters.BANK_NAME, bankId)).get(responseClass);
    }

    public String initLogin() {
        return client.request(Urls.INIT_LOGIN)
                .queryParam("bank", "fid-sparebank1")
                .queryParam("login-method", "bim")
                .queryParam("goto", "https://www.sparebank1.no/login-complete-redirect")
                .get(String.class);
    }

    public String selectMarketAndAuthentication(String bankIdBody) {
        return client.request(Urls.SELECT_MARKET_AND_AUTH_TYPE)
                .queryParam(Parameters.CID, Parameters.CID_VALUE)
                .type(MediaType.APPLICATION_FORM_URLENCODED)
                .post(String.class, bankIdBody);
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

    public CreditCardTransactionsResponse fetchCreditCardTransactions(String bankIdentifier) {
        return client.request(
                        Urls.CREDITCARD_TRANSACTIONS
                                .parameter(Parameters.BANK_NAME, bankId)
                                .parameter(Parameters.ACCOUNT_ID, bankIdentifier))
                .get(CreditCardTransactionsResponse.class);
    }

    public LoanDetailsEntity fetchLoanDetails(String loanId) {
        return client.request(
                        Urls.LOAN_DETAILS
                                .parameter(Parameters.BANK_NAME, bankId)
                                .parameter(Parameters.ACCOUNT_ID, loanId))
                .queryParam(QueryParams.UNDERSCORE, String.valueOf(System.currentTimeMillis()))
                .get(LoanDetailsEntity.class);
    }

    public TransactionsResponse fetchTransactions(String url) {
        return client.request(url).get(TransactionsResponse.class);
    }

    public HttpResponse logout() {
        return getJsonSessionRequestBuilder(Urls.SESSION).delete(HttpResponse.class);
    }

    public void requestDigitalSession() {
        client.request(Urls.DIGITAL_SESSION)
                .accept(Headers.TEXT_HTML_APPLICATION_XHTML_XML)
                .type(MediaType.WILDCARD)
                .get(HttpResponse.class);
    }

    public void retrieveSessionCookie() {
        client.request(Urls.INITIAL_REQUEST)
                .type(MediaType.WILDCARD)
                .accept(MediaType.WILDCARD)
                .get(HttpResponse.class);

        this.sessionToken =
                client.getCookies().stream()
                        .filter(cookie -> Keys.SESSION_ID.equalsIgnoreCase(cookie.getName()))
                        .findFirst()
                        .orElseThrow(() -> new IllegalStateException("Dsession cookie not found"))
                        .getValue();
    }

    public RequestBuilder getSessionRequestBuilder(URL url) {
        return client.request(url)
                .header(Headers.CSRFT_TOKEN, sessionToken)
                .header(Headers.X_SB1_REST_VERSION, Headers.X_SB1_REST_VERSION_VALUE);
    }

    public RequestBuilder getJsonSessionRequestBuilder(URL url) {
        return getSessionRequestBuilder(url)
                .accept(Headers.APPLICATION_JSON_CHARSET_UTF8)
                .type(MediaType.APPLICATION_JSON);
    }

    public BankBranchResponse getUserBranches() {
        return getJsonSessionRequestBuilder(Urls.BRANCHES).get(BankBranchResponse.class);
    }

    public void setSpecificUserBranch(Branch request) {
        getJsonSessionRequestBuilder(Urls.BRANCHES).post(request);
    }

    public AgreementsResponse getAgreements() {
        return getJsonSessionRequestBuilder(Urls.AGREEMENTS).get(AgreementsResponse.class);
    }

    public void setSpecificAgreement(String agreementId) {
        AgreementRequest agreementRequest = new AgreementRequest(agreementId);
        getJsonSessionRequestBuilder(Urls.AGREEMENTS).put(agreementRequest);
    }

    public TokenExpirationTimeResponse requestForTokenExpirationTimestamp() {
        return getSessionRequestBuilder(Urls.TOKEN)
                .type(Headers.V_2_JSON)
                .accept(Headers.V_2_JSON)
                .get(TokenExpirationTimeResponse.class);
    }

    public TokenResponse requestForToken(InitTokenRequest signedJwt) {
        return getSessionRequestBuilder(Urls.TOKEN)
                .type(Headers.V_3_JSON)
                .accept(Headers.V_3_JSON)
                .post(TokenResponse.class, signedJwt);
    }

    public InitSessionResponse initiateSession(InitSessionRequest authenticationRequest) {
        return getJsonSessionRequestBuilder(Urls.SESSION)
                .post(InitSessionResponse.class, authenticationRequest);
    }

    public SessionResponse finishSessionInitiation(SessionRequest request) throws SessionException {
        return getJsonSessionRequestBuilder(Urls.SESSION).put(SessionResponse.class, request);
    }
}
