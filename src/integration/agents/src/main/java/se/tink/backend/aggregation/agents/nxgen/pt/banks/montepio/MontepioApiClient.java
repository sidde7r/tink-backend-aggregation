package se.tink.backend.aggregation.agents.nxgen.pt.banks.montepio;

import static java.util.Objects.requireNonNull;

import java.time.LocalDate;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.montepio.MontepioConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.montepio.MontepioConstants.HeaderValues;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.montepio.MontepioConstants.PropertyKeys;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.montepio.MontepioConstants.URLs;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.montepio.authenticator.PasswordEncryptionUtil;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.montepio.authenticator.rpc.AuthenticationRequest;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.montepio.authenticator.rpc.AuthenticationResponse;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.montepio.entities.FetchAccountDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.montepio.fetcher.creditcard.rpc.FetchCreditCardResponse;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.montepio.rpc.FetchAccountDetailsRequest;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.montepio.rpc.FetchAccountTransactionsRequest;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.montepio.rpc.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.montepio.rpc.FetchCreditCardTransactionsRequest;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.montepio.rpc.FetchTransactionsResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class MontepioApiClient {

    private static final String EMPTY_JSON = "{}";

    private final TinkHttpClient httpClient;

    MontepioApiClient(TinkHttpClient httpClient) {
        this.httpClient = requireNonNull(httpClient);
    }

    private RequestBuilder baseRequest(URL url) {
        return httpClient
                .request(url)
                .type(MediaType.APPLICATION_JSON)
                .acceptLanguage(HeaderValues.ACCEPT_LANGUAGE)
                .accept(HeaderValues.ACCEPT)
                .header(HttpHeaders.ACCEPT_ENCODING, HeaderValues.ACCEPT_ENCODING)
                .header(HeaderKeys.APP_VERSION, HeaderValues.APP_VERSION)
                .header(HeaderKeys.APP_ID, HeaderValues.APP_ID)
                .header(HeaderKeys.DEVICE, HeaderValues.DEVICE)
                .header(HeaderKeys.LANG, HeaderValues.LANG)
                .header(HeaderKeys.PSU_IP, HeaderValues.PSU_IP)
                .header(HeaderKeys.IOS_VERSION, HeaderValues.IOS_VERSION)
                .header(HeaderKeys.MGM_VERSION, HeaderValues.MGM_VERSION);
    }

    public AuthenticationStepResponse loginStep0(String username, String password)
            throws LoginException {
        String maskedPassword = PasswordEncryptionUtil.encryptPassword(username, password);
        AuthenticationRequest request = new AuthenticationRequest(username, maskedPassword);
        AuthenticationResponse response =
                baseRequest(URLs.LOGIN).post(AuthenticationResponse.class, request);
        if (response.isWrongCredentials()) {
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }
        return AuthenticationStepResponse.executeNextStep();
    }

    public void loginStep1() {
        baseRequest(URLs.FINALIZE_LOGIN).post(EMPTY_JSON);
    }

    public FetchAccountsResponse fetchAccounts() {
        return baseRequest(URLs.FETCH_ACCOUNTS)
                .header(HeaderKeys.SCREEN_NAME, HeaderValues.ACCOUNTS_SCREEN_NAME)
                .post(FetchAccountsResponse.class, EMPTY_JSON);
    }

    public FetchAccountDetailsResponse fetchAccountDetails(String handle) {
        FetchAccountDetailsRequest request = new FetchAccountDetailsRequest(handle);
        return baseRequest(URLs.FETCH_ACCOUNT_DETAILS)
                .header(HeaderKeys.SCREEN_NAME, HeaderValues.ACCOUNTS_SCREEN_NAME)
                .post(FetchAccountDetailsResponse.class, request);
    }

    public FetchCreditCardResponse fetchCreditCards() {
        return baseRequest(URLs.FETCH_CREDIT_CARDS).post(FetchCreditCardResponse.class);
    }

    public FetchAccountDetailsResponse fetchCreditCardDetails(String handle) {
        FetchAccountDetailsRequest request = new FetchAccountDetailsRequest(handle);
        return baseRequest(URLs.FETCH_CREDIT_CARD_DETAILS)
                .header(HeaderKeys.SCREEN_NAME, HeaderValues.ACCOUNTS_SCREEN_NAME)
                .post(FetchAccountDetailsResponse.class, request);
    }

    public FetchTransactionsResponse fetchCheckingAccountTransactions(
            Account account, int pageNumber, LocalDate from, LocalDate to) {
        return fetchAccountTransactions(URLs.FETCH_TRANSACTIONS, account, pageNumber, from, to);
    }

    public FetchAccountsResponse fetchSavingsAccounts() {
        return baseRequest(URLs.FETCH_SAVINGS_ACCOUNTS)
                .header(HeaderKeys.SCREEN_NAME, HeaderValues.SAVINGS_ACCOUNTS_SCREEN_NAME)
                .post(FetchAccountsResponse.class, EMPTY_JSON);
    }

    public FetchTransactionsResponse fetchSavingsAccountTransactions(
            Account account, int pageNumber, LocalDate from, LocalDate to) {
        return fetchAccountTransactions(
                URLs.FETCH_SAVINGS_ACCOUNT_TRANSACTIONS, account, pageNumber, from, to);
    }

    public FetchTransactionsResponse fetchCreditCardTransactions(
            Account account, int pageNumber, LocalDate from, LocalDate to) {
        String handle = account.getFromTemporaryStorage(PropertyKeys.HANDLE);
        FetchCreditCardTransactionsRequest request =
                new FetchCreditCardTransactionsRequest(pageNumber, to, from, handle);
        return baseRequest(URLs.FETCH_CREDIT_CARD_TRANSACTIONS)
                .header(HeaderKeys.SCREEN_NAME, HeaderValues.TRANSACTIONS_SCREEN_NAME)
                .post(FetchTransactionsResponse.class, request);
    }

    private FetchTransactionsResponse fetchAccountTransactions(
            URL url, Account account, int pageNumber, LocalDate from, LocalDate to) {
        String handle = account.getFromTemporaryStorage(PropertyKeys.HANDLE);
        FetchAccountTransactionsRequest request =
                new FetchAccountTransactionsRequest(pageNumber, to, from, handle);
        return baseRequest(url)
                .header(HeaderKeys.SCREEN_NAME, HeaderValues.TRANSACTIONS_SCREEN_NAME)
                .post(FetchTransactionsResponse.class, request);
    }

    public FetchAccountsResponse fetchLoans() {
        return baseRequest(URLs.FETCH_LOAN_ACCOUNTS).post(FetchAccountsResponse.class, EMPTY_JSON);
    }

    public FetchAccountDetailsResponse fetchLoanDetails(String handle) {
        return baseRequest(URLs.FETCH_LOAN_ACCOUNT_DETAILS)
                .post(FetchAccountDetailsResponse.class, new FetchAccountDetailsRequest(handle));
    }
}
