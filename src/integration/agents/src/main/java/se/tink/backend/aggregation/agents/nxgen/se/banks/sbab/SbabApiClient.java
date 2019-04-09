package se.tink.backend.aggregation.agents.nxgen.se.banks.sbab;

import java.util.Date;
import java.util.Optional;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.SbabConstants.Environment;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.SbabConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.SbabConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.SbabConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.SbabConstants.Uris;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.authenticator.entities.AuthGrantTypeEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.authenticator.entities.AuthMethodEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.authenticator.entities.AuthResponseTypeEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.authenticator.entities.AuthScopeEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.authenticator.rpc.AccessTokenRequest;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.authenticator.rpc.AccessTokenResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.authenticator.rpc.PendingAuthCodeRequest;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.configuration.SbabConfiguration;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.fetcher.loan.rpc.LoanResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.fetcher.loan.rpc.LoansResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.fetcher.savingsaccount.rpc.AccountResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.fetcher.savingsaccount.rpc.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.fetcher.savingsaccount.rpc.TransferDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.fetcher.savingsaccount.rpc.TransferRequest;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.fetcher.savingsaccount.rpc.TransferRequestStatusResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.fetcher.savingsaccount.rpc.TransfersResponse;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.date.ThreadSafeDateFormat;

public final class SbabApiClient {
    private final TinkHttpClient client;
    private final SessionStorage sessionStorage;
    private SbabConfiguration configuration;

    public SbabApiClient(TinkHttpClient client, SessionStorage sessionStorage) {
        this.client = client;
        this.sessionStorage = sessionStorage;
    }

    public SbabConfiguration getConfiguration() {
        return Optional.ofNullable(configuration)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_CONFIGURATION));
    }

    public void setConfiguration(SbabConfiguration configuration) {
        this.configuration = configuration;
    }

    private RequestBuilder createRequest(String uri) {
        final Environment environment = getConfiguration().getEnvironment();
        final String url = Uris.GET_BASE_URL(environment) + uri;

        return client.request(url)
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON);
    }

    private RequestBuilder createRequestInSession(String uri) {
        final Environment environment = getConfiguration().getEnvironment();
        final OAuth2Token token;

        switch (environment) {
            case SANDBOX:
                final String accessToken = sessionStorage.get(StorageKeys.ACCESS_TOKEN);
                token = OAuth2Token.createBearer(accessToken, null, 1600);

                return createRequest(uri).addBearerToken(token);
            default:
                final String username = getConfiguration().getBasicAuthUsername();
                final String password = getConfiguration().getBasicAuthPassword();

                token =
                        sessionStorage
                                .get(StorageKeys.OAUTH2_TOKEN, OAuth2Token.class)
                                .orElseThrow(
                                        () ->
                                                new IllegalStateException(
                                                        SessionError.SESSION_EXPIRED.exception()));

                return createRequest(uri).addBasicAuth(username, password).addBearerToken(token);
        }
    }

    public URL buildAuthorizeUrl(String state) {
        final String clientId = getConfiguration().getClientId();
        final String redirectUri = getConfiguration().getRedirectUri();

        final PendingAuthCodeRequest request =
                new PendingAuthCodeRequest()
                        .withClientId(clientId)
                        .withResponseType(AuthResponseTypeEntity.PENDING_CODE)
                        .withRedirectUri(redirectUri)
                        .withScope(AuthScopeEntity.ACCOUNT_LOAN_READ)
                        .withState(state)
                        .withAuthMethod(AuthMethodEntity.MOBILE_BANKID)
                        .withUserId(sessionStorage.get(StorageKeys.USER_ID));

        return new URL(Uris.GET_PENDING_AUTH_CODE()).queryParams(request);
    }

    public String getAuthorizationStatus(String pendingAuthCode) {
        final String uri = SbabConstants.Uris.GET_AUTH_STATUS(pendingAuthCode);

        return createRequest(uri).get(String.class);
    }

    public AccessTokenResponse getAccessToken(String pendingCode) {
        final String redirectUri = getConfiguration().getRedirectUri();
        final String uri = Uris.GET_ACCESS_TOKEN();

        final AccessTokenRequest accessTokenRequest =
                new AccessTokenRequest()
                        .withGrantType(AuthGrantTypeEntity.PENDING_AUTHORIZATION_CODE)
                        .withPendingCode(pendingCode)
                        .withRedirectUri(redirectUri);

        return createRequest(uri).body(accessTokenRequest).post(AccessTokenResponse.class);
    }

    public AccountsResponse listAccounts() {
        final String uri = Uris.LIST_ACCOUNTS();

        return createRequestInSession(uri).get(AccountsResponse.class);
    }

    public AccountResponse getAccount(String accountNumber) {
        final String uri = SbabConstants.Uris.GET_ACCOUNT(accountNumber);

        return createRequestInSession(uri).get(AccountResponse.class);
    }

    public TransferDetailsResponse getTransfer(String accountNumber, String transferId) {
        final String uri = Uris.GET_TRANSFER(accountNumber, transferId);

        return createRequestInSession(uri).get(TransferDetailsResponse.class);
    }

    public HttpResponse deleteTransfer(String accountNumber, String transferId) {
        final String uri = SbabConstants.Uris.DELETE_TRANSFER(accountNumber, transferId);

        return createRequestInSession(uri).delete(HttpResponse.class);
    }

    public TransferRequestStatusResponse initiateTransfer(
            String accountNumber, TransferRequest transferRequest) {
        final String uri = SbabConstants.Uris.INIT_TRANSFER(accountNumber);

        return createRequestInSession(uri)
                .body(transferRequest)
                .get(TransferRequestStatusResponse.class);
    }

    public TransfersResponse listTransfers(String accountNumber, Date fromDate, Date toDate) {
        final String startDate = ThreadSafeDateFormat.FORMATTER_DAILY.format(fromDate);
        final String endDate = ThreadSafeDateFormat.FORMATTER_DAILY.format(toDate);
        final URL uri =
                new URL(Uris.LIST_TRANSFERS(accountNumber))
                        .queryParam(QueryKeys.START_DATE, startDate)
                        .queryParam(QueryKeys.END_DATE, endDate);

        return createRequestInSession(uri.toString()).get(TransfersResponse.class);
    }

    public TransferRequestStatusResponse getTransferStatus(
            String accountNumber, String referenceId) {
        final String uri = SbabConstants.Uris.GET_TRANSFER_STATUS(accountNumber, referenceId);

        return createRequestInSession(uri).get(TransferRequestStatusResponse.class);
    }

    public LoansResponse listLoans() {
        final String uri = SbabConstants.Uris.LIST_LOANS();

        return createRequestInSession(uri).get(LoansResponse.class);
    }

    public LoanResponse getLoan(String loanNumber) {
        final String uri = SbabConstants.Uris.GET_LOAN(loanNumber);

        return createRequestInSession(uri).get(LoanResponse.class);
    }
}
