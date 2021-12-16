package se.tink.backend.aggregation.agents.nxgen.se.other.csn;

import java.util.Optional;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.se.other.csn.CSNConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.se.other.csn.CSNConstants.HeaderValues;
import se.tink.backend.aggregation.agents.nxgen.se.other.csn.CSNConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.se.other.csn.authenticator.bankid.rpc.LoginForm;
import se.tink.backend.aggregation.agents.nxgen.se.other.csn.fetcher.loans.rpc.LoanAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.se.other.csn.fetcher.loans.rpc.LoanTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.se.other.csn.fetcher.loans.rpc.UserInfoResponse;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

@Slf4j
@RequiredArgsConstructor
public class CSNApiClient {
    private final TinkHttpClient client;
    private final CSNAuthSessionStorageHelper authSessionStorageHelper;

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
                        () ->
                                LoginError.INCORRECT_CREDENTIALS.exception(
                                        "Required value access_token is missing"));
    }

    public HttpResponse initBankId(LoginForm loginForm) {
        verifyConnection();
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
        return createRequestInSession(CSNConstants.Urls.CURRENT_DEBT)
                .get(LoanAccountsResponse.class);
    }

    public LoanTransactionsResponse fetchLoanTransactions() {
        return createRequestInSession(CSNConstants.Urls.LOAN_TRANSACTIONS)
                .get(LoanTransactionsResponse.class);
    }

    public UserInfoResponse fetchUserInfo() {
        return createRequestInSession(CSNConstants.Urls.USER_INFO).get(UserInfoResponse.class);
    }

    private RequestBuilder createRequestInSession(String url) {
        Optional<String> accessToken = authSessionStorageHelper.getAccessToken();

        if (!accessToken.isPresent()) {
            throw new IllegalStateException("Expected access token to be present!");
        }

        return client.request(url)
                .type(MediaType.APPLICATION_JSON)
                .header(
                        CSNConstants.HeaderKeys.CSN_AUTHORIZATION,
                        CSNConstants.HeaderValues.BEARER + accessToken.get());
    }

    private void verifyConnection() {
        HttpResponse response =
                client.request(Urls.VERIFY_URL)
                        .header(HeaderKeys.REFERER_POLICY, HeaderValues.CROSS_ORIGIN)
                        .type(MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                        .post(HttpResponse.class);
        if (response.getStatus() != HttpStatus.SC_OK) {
            log.error("Response status: " + response.getStatus());
            throw BankServiceError.BANK_SIDE_FAILURE.exception();
        }
    }
}
