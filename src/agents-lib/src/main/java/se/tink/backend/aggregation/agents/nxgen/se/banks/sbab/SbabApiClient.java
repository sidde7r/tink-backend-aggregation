package se.tink.backend.aggregation.agents.nxgen.se.banks.sbab;

import java.util.Date;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.SbabConstants.Environment;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.SbabConstants.QueryKey;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.SbabConstants.StorageKey;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.SbabConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.authenticator.rpc.AccessTokenRequest;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.authenticator.rpc.AccessTokenResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.authenticator.rpc.InvalidateTokenRequest;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.authenticator.rpc.InvalidateTokenResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.fetcher.loan.rpc.LoanResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.fetcher.loan.rpc.LoansResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.fetcher.transactionalaccount.rpc.AccountResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.fetcher.transactionalaccount.rpc.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.fetcher.transactionalaccount.rpc.TransferDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.fetcher.transactionalaccount.rpc.TransferRequest;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.fetcher.transactionalaccount.rpc.TransferRequestStatusResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.fetcher.transactionalaccount.rpc.TransfersResponse;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.date.ThreadSafeDateFormat;

public final class SbabApiClient {
    private final TinkHttpClient client;
    private final Environment environment;
    private final PersistentStorage persistentStorage;
    private final SessionStorage sessionStorage;

    public SbabApiClient(
            TinkHttpClient client,
            Environment environment,
            SessionStorage sessionStorage,
            PersistentStorage persistentStorage) {
        this.client = client;
        this.environment = environment;
        this.persistentStorage = persistentStorage;
        this.sessionStorage = sessionStorage;
    }

    private RequestBuilder createRequest(URL url) {
        return client.request(url)
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON);
    }

    private RequestBuilder createRequest(String url) {
        return createRequest(new URL(url));
    }

    private RequestBuilder createRequestInSession(URL url) {
        final OAuth2Token token;

        switch (environment) {
            case SANDBOX:
                final String accessToken = sessionStorage.get(StorageKey.ACCESS_TOKEN);
                token = OAuth2Token.createBearer(accessToken, null, 1600);

                return createRequest(url).addBearerToken(token);
            default:
                final String username = persistentStorage.get(StorageKey.BASIC_AUTH_USERNAME);
                final String password = persistentStorage.get(StorageKey.BASIC_AUTH_PASSWORD);

                token =
                        sessionStorage
                                .get(StorageKey.OAUTH2_TOKEN, OAuth2Token.class)
                                .orElseThrow(
                                        () ->
                                                new IllegalStateException(
                                                        SessionError.SESSION_EXPIRED.exception()));

                return createRequest(url).addBasicAuth(username, password).addBearerToken(token);
        }
    }

    private RequestBuilder createRequestInSession(String url) {
        return createRequestInSession(new URL(url));
    }

    public String getAuthorizationStatus(String pendingAuthCode) {
        final String url = Urls.GET_AUTH_STATUS(environment, pendingAuthCode);

        return createRequest(url).get(String.class);
    }

    public InvalidateTokenResponse invalidateAccessToken(
            InvalidateTokenRequest invalidateTokenRequest) {
        final String url = Urls.INVALIDATE_ACCESS_TOKEN(environment);

        return createRequest(url).body(invalidateTokenRequest).post(InvalidateTokenResponse.class);
    }

    public AccessTokenResponse getAccessToken(AccessTokenRequest accessTokenRequest) {
        final String url = Urls.GET_ACCESS_TOKEN(environment);

        return createRequest(url).body(accessTokenRequest).post(AccessTokenResponse.class);
    }

    public AccountsResponse listAccounts() {
        final String url = Urls.LIST_ACCOUNTS(environment);

        return createRequestInSession(url).get(AccountsResponse.class);
    }

    public AccountResponse getAccount(String accountNumber) {
        final String url = Urls.GET_ACCOUNT(environment, accountNumber);

        return createRequestInSession(url).get(AccountResponse.class);
    }

    public TransferDetailsResponse getTransfer(String accountNumber, String transferId) {
        final String url = Urls.GET_TRANSFER(environment, accountNumber, transferId);

        return createRequestInSession(url).get(TransferDetailsResponse.class);
    }

    public HttpResponse deleteTransfer(String accountNumber, String transferId) {
        final String url = Urls.DELETE_TRANSFER(environment, accountNumber, transferId);

        return createRequestInSession(url).delete(HttpResponse.class);
    }

    public TransferRequestStatusResponse initiateTransfer(
            String accountNumber, TransferRequest transferRequest) {
        final String url = Urls.INIT_TRANSFER(environment, accountNumber);

        return createRequestInSession(url)
                .body(transferRequest)
                .get(TransferRequestStatusResponse.class);
    }

    public TransfersResponse listTransfers(String accountNumber, Date fromDate, Date toDate) {
        final String startDate = ThreadSafeDateFormat.FORMATTER_DAILY.format(fromDate);
        final String endDate = ThreadSafeDateFormat.FORMATTER_DAILY.format(toDate);
        final URL url =
                new URL(Urls.LIST_TRANSFERS(environment, accountNumber))
                        .queryParam(QueryKey.START_DATE, startDate)
                        .queryParam(QueryKey.END_DATE, endDate);

        return createRequestInSession(url).get(TransfersResponse.class);
    }

    public TransferRequestStatusResponse getTransferStatus(
            String accountNumber, String referenceId) {
        final String url = Urls.GET_TRANSFER_STATUS(environment, accountNumber, referenceId);

        return createRequestInSession(url).get(TransferRequestStatusResponse.class);
    }

    public LoansResponse listLoans() {
        final String url = Urls.LIST_LOANS(environment);

        return createRequestInSession(url).get(LoansResponse.class);
    }

    public LoanResponse getLoan(String loanNumber) {
        final String url = Urls.GET_LOAN(environment, loanNumber);

        return createRequestInSession(url).get(LoanResponse.class);
    }
}
