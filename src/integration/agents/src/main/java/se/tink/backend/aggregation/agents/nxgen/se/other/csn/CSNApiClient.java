package se.tink.backend.aggregation.agents.nxgen.se.other.csn;

import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.se.other.csn.authenticator.bankid.rpc.LoginForm;
import se.tink.backend.aggregation.agents.nxgen.se.other.csn.fetcher.loans.rpc.LoanAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.se.other.csn.fetcher.loans.rpc.LoanTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.se.other.csn.fetcher.loans.rpc.UserInfoResponse;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

@RequiredArgsConstructor
public class CSNApiClient {
    private final TinkHttpClient client;
    private final SessionStorage sessionStorage;

    public String extractSessionId(HttpResponse httpResponse) {
        return httpResponse.getCookies().stream()
                .filter(
                        cookie ->
                                CSNConstants.Storage.SESSION_ID.equalsIgnoreCase(cookie.getName()))
                .findFirst()
                .map(Cookie::getValue)
                .orElseThrow(
                        () -> new IllegalStateException("Required value JSESSIONID is missing"));
    }

    public String extractAccessToken() {
        return client.getCookies().stream()
                .filter(
                        cookie ->
                                CSNConstants.Storage.ACCESS_TOKEN.equalsIgnoreCase(
                                        cookie.getName()))
                .findFirst()
                .map(cookie -> cookie.getValue())
                .orElseThrow(
                        () -> new IllegalStateException("Required value access_token is missing"));
    }

    public HttpResponse initBankId(LoginForm loginForm) {
        return client.request(CSNConstants.Urls.LOGIN_BANKID)
                .type(MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                .post(HttpResponse.class, loginForm);
    }

    public HttpResponse pollBankId() {
        return client.request(CSNConstants.Urls.BANKID_POLL)
                .accept(MediaType.TEXT_PLAIN)
                .header(CSNConstants.HeaderKeys.REFERER, CSNConstants.Urls.LOGIN_BANKID)
                .get(HttpResponse.class);
    }

    public LoanAccountsResponse fetchLoanAccounts() {
        return client.request(CSNConstants.Urls.CURRENT_DEBT)
                .type(MediaType.APPLICATION_JSON)
                .header(
                        CSNConstants.HeaderKeys.CSN_AUTHORIZATION,
                        CSNConstants.HeaderValues.BEARER
                                + sessionStorage.get(CSNConstants.Storage.ACCESS_TOKEN))
                .get(LoanAccountsResponse.class);
    }

    public LoanTransactionsResponse fetchLoanTransactions() {
        return client.request(CSNConstants.Urls.LOAN_TRANSACTIONS)
                .type(MediaType.APPLICATION_JSON)
                .header(
                        CSNConstants.HeaderKeys.CSN_AUTHORIZATION,
                        CSNConstants.HeaderValues.BEARER
                                + sessionStorage.get(CSNConstants.Storage.ACCESS_TOKEN))
                .get(LoanTransactionsResponse.class);
    }

    public UserInfoResponse fetchUserInfo() {
        return client.request(CSNConstants.Urls.USER_INFO)
                .type(MediaType.APPLICATION_JSON)
                .header(
                        CSNConstants.HeaderKeys.CSN_AUTHORIZATION,
                        CSNConstants.HeaderValues.BEARER
                                + sessionStorage.get(CSNConstants.Storage.ACCESS_TOKEN))
                .get(UserInfoResponse.class);
    }
}
