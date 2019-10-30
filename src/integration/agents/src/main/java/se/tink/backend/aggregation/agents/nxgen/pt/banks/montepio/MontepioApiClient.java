package se.tink.backend.aggregation.agents.nxgen.pt.banks.montepio;

import static java.util.Objects.requireNonNull;

import java.time.LocalDate;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.montepio.authenticator.PasswordEncryptionUtil;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.montepio.authenticator.rpc.AuthenticationRequest;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.montepio.authenticator.rpc.AuthenticationResponse;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.montepio.fetcher.accounts.rpc.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.montepio.fetcher.accounts.rpc.FetchTransactionsRequest;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.montepio.fetcher.accounts.rpc.FetchTransactionsResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class MontepioApiClient {

    private static final String EMPTY_JSON = "{}";

    private static final String ACCOUNT_HANDLE_SESSION_STORAGE_PATTERN = "ACCOUNT_HANDLE:%s";

    private final TinkHttpClient httpClient;
    private final SessionStorage sessionStorage;

    MontepioApiClient(final TinkHttpClient httpClient, final SessionStorage sessionStorage) {
        this.httpClient = requireNonNull(httpClient);
        this.sessionStorage = requireNonNull(sessionStorage);
    }

    private RequestBuilder baseRequest(URL url) {
        return httpClient
                .request(url)
                .type(MediaType.APPLICATION_JSON)
                .acceptLanguage(MontepioConstants.HeaderValues.ACCEPT_LANGUAGE)
                .accept(MontepioConstants.HeaderValues.ACCEPT)
                .header(HttpHeaders.ACCEPT_ENCODING, MontepioConstants.HeaderValues.ACCEPT_ENCODING)
                .header(
                        MontepioConstants.HeaderKeys.APP_VERSION,
                        MontepioConstants.HeaderValues.APP_VERSION)
                .header(MontepioConstants.HeaderKeys.APP_ID, MontepioConstants.HeaderValues.APP_ID)
                .header(MontepioConstants.HeaderKeys.DEVICE, MontepioConstants.HeaderValues.DEVICE)
                .header(MontepioConstants.HeaderKeys.LANG, MontepioConstants.HeaderValues.LANG)
                .header(MontepioConstants.HeaderKeys.PSU_IP, MontepioConstants.HeaderValues.PSU_IP)
                .header(
                        MontepioConstants.HeaderKeys.IOS_VERSION,
                        MontepioConstants.HeaderValues.IOS_VERSION)
                .header(
                        MontepioConstants.HeaderKeys.MGM_VERSION,
                        MontepioConstants.HeaderValues.MGM_VERSION);
    }

    public void loginStep0(String username, String password) throws LoginException {
        String maskedPassword = PasswordEncryptionUtil.encryptPassword(username, password);
        AuthenticationRequest request = new AuthenticationRequest(username, maskedPassword);
        AuthenticationResponse response =
                baseRequest(MontepioConstants.URLs.LOGIN)
                        .post(AuthenticationResponse.class, request);
        if (response.isWrongCredentials()) {
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }
    }

    public void loginStep1() {
        baseRequest(MontepioConstants.URLs.FINALIZE_LOGIN).post(EMPTY_JSON);
    }

    public FetchAccountsResponse fetchAccounts() {
        return baseRequest(MontepioConstants.URLs.FETCH_ACCOUNTS)
                .header(
                        MontepioConstants.HeaderKeys.SCREEN_NAME,
                        MontepioConstants.HeaderValues.ACCOUNTS_SCREEN_NAME)
                .post(FetchAccountsResponse.class, EMPTY_JSON);
    }

    public FetchTransactionsResponse fetchTransactions(
            TransactionalAccount account, int pageNumber, LocalDate from, LocalDate to) {
        String handle = account.getFromTemporaryStorage(MontepioConstants.PropertyKeys.HANDLE);
        FetchTransactionsRequest request =
                new FetchTransactionsRequest(pageNumber, to, from, handle);
        return baseRequest(MontepioConstants.URLs.FETCH_TRANSACTIONS)
                .header(
                        MontepioConstants.HeaderKeys.SCREEN_NAME,
                        MontepioConstants.HeaderValues.TRANSACTIONS_SCREEN_NAME)
                .post(FetchTransactionsResponse.class, request);
    }
}
