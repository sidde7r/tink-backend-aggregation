package se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki;

import java.util.UUID;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.authenticator.rpc.AddDeviceRequest;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.authenticator.rpc.AddDeviceResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.authenticator.rpc.LoginRequest;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.authenticator.rpc.PinLoginResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.authenticator.rpc.RequestChallengeRequest;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.authenticator.rpc.RequestChallengeResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.authenticator.rpc.RespondToChallengeRequest;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.authenticator.rpc.TokenLoginResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.authenticator.rpc.UsernamePasswordLoginResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.fetcher.investment.rpc.AllFundsRequest;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.fetcher.investment.rpc.AllFundsResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.fetcher.investment.rpc.FundsPortfoliosResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.fetcher.loan.entities.LoanDetailsIdentity;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.fetcher.loan.rpc.LoanDetailsRequest;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.fetcher.loan.rpc.LoanDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.fetcher.loan.rpc.LoanOverviewRequest;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.fetcher.loan.rpc.LoanOverviewResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.fetcher.transactionalaccount.rpc.GetAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.fetcher.transactionalaccount.rpc.GetTransactionsRequest;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.fetcher.transactionalaccount.rpc.GetTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.fetcher.transactionalaccount.rpc.ReservationsRequest;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.fetcher.transactionalaccount.rpc.ReservationsResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.rpc.SpankkiRequest;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.rpc.SpankkiResponse;
import se.tink.backend.aggregation.agents.utils.crypto.Hash;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;

public class SpankkiApiClient {

    private final TinkHttpClient client;
    private final SpankkiSessionStorage sessionStorage;
    private final SpankkiPersistentStorage persistentStorage;

    public SpankkiApiClient(
            TinkHttpClient client,
            SpankkiSessionStorage sessionStorage,
            SpankkiPersistentStorage persistentStorage) {
        this.client = client;
        this.sessionStorage = sessionStorage;
        this.persistentStorage = persistentStorage;
    }

    public SpankkiResponse handleSetupChallenge() {
        String randomString = UUID.randomUUID().toString();
        String requestToken = calculateRequestToken(randomString);

        RequestChallengeRequest reqChallengeRequest =
                new RequestChallengeRequest().setRequestInfo(randomString);

        addSessionIdAndRequestToken(reqChallengeRequest, requestToken);

        RequestChallengeResponse reqChallengeResponse = requestChallenge(reqChallengeRequest);

        RespondToChallengeRequest resChallengeRequest =
                new RespondToChallengeRequest()
                        .setAuthenticationId(reqChallengeResponse.getAuthenticationId())
                        .setChallengeResponse(
                                calculateChallengeResponse(reqChallengeResponse.getChallenge()));
        addSessionIdAndRequestToken(resChallengeRequest, requestToken);

        return respondToChallenge(resChallengeRequest);
    }

    public UsernamePasswordLoginResponse loginUserPassword(String username, String password)
            throws AuthenticationException, AuthorizationException {
        LoginRequest loginRequest =
                LoginRequest.createUsernamePasswordLoginRequest(username, password);
        addSessionIdAndRequestToken(
                loginRequest, SpankkiConstants.Url.LOGIN_USERNAME_PASSWORD.getRequestToken());

        return postLoginRequest(
                UsernamePasswordLoginResponse.class,
                SpankkiConstants.Url.LOGIN_USERNAME_PASSWORD.getUrl(),
                loginRequest);
    }

    public PinLoginResponse loginPin(String code)
            throws AuthenticationException, AuthorizationException {
        LoginRequest loginRequest = LoginRequest.createPinLoginRequest(code);
        addSessionIdAndRequestToken(loginRequest, SpankkiConstants.Url.LOGIN_PIN.getRequestToken());

        return postLoginRequest(
                PinLoginResponse.class, SpankkiConstants.Url.LOGIN_PIN.getUrl(), loginRequest);
    }

    public AddDeviceResponse addDevice() {
        String deviceId = UUID.randomUUID().toString();

        AddDeviceRequest addDeviceRequest =
                new AddDeviceRequest()
                        .setUserDeviceName(SpankkiConstants.Authentication.USER_DEVICE_NAME)
                        .setHardwareId(deviceId);
        addSessionIdAndRequestToken(
                addDeviceRequest, SpankkiConstants.Url.ADD_DEVICE.getRequestToken());

        return postRequest(
                AddDeviceResponse.class,
                SpankkiConstants.Url.ADD_DEVICE.getUrl(),
                addDeviceRequest);
    }

    public TokenLoginResponse loginWithToken(String password, String deviceId, String deviceToken)
            throws AuthenticationException, AuthorizationException {
        LoginRequest loginRequest =
                LoginRequest.createDeviceTokenLoginRequest(password, deviceId, deviceToken);
        addSessionIdAndRequestToken(
                loginRequest, SpankkiConstants.Url.LOGIN_DEVICE_TOKEN.getRequestToken());

        return postLoginRequest(
                TokenLoginResponse.class,
                SpankkiConstants.Url.LOGIN_DEVICE_TOKEN.getUrl(),
                loginRequest);
    }

    public GetAccountsResponse fetchAccounts() {
        SpankkiRequest getAccountsRequest = new SpankkiRequest();
        addSessionIdRequestTokenAndDeviceId(
                getAccountsRequest, SpankkiConstants.Url.GET_ACCOUNTS.getRequestToken());

        return postRequest(
                GetAccountsResponse.class,
                SpankkiConstants.Url.GET_ACCOUNTS.getUrl(),
                getAccountsRequest);
    }

    public GetTransactionsResponse fetchTransactions(
            String bankId, String fromDate, String toDate) {
        GetTransactionsRequest getTransactionsRequest =
                new GetTransactionsRequest()
                        .setAccountId(bankId)
                        .setFromDate(fromDate)
                        .setToDate(toDate);
        addSessionIdRequestTokenAndDeviceId(
                getTransactionsRequest, SpankkiConstants.Url.GET_TRANSACTIONS.getRequestToken());

        return postRequest(
                GetTransactionsResponse.class,
                SpankkiConstants.Url.GET_TRANSACTIONS.getUrl(),
                getTransactionsRequest);
    }

    public ReservationsResponse fetchReservations(String bankId) {
        ReservationsRequest reservationsRequest =
                new ReservationsRequest().setReservationAccountId(bankId);
        addSessionIdAndDeviceId(reservationsRequest);

        return postRequest(
                ReservationsResponse.class,
                SpankkiConstants.Url.RESERVATIONS.getUrl(),
                reservationsRequest);
    }

    public void logout() {
        SpankkiRequest logoutRequest = new SpankkiRequest();
        addSessionIdAndDeviceId(logoutRequest);

        request(SpankkiConstants.Url.LOGOUT.getUrl()).post(logoutRequest);
    }

    // logging call
    public String fetchCardsOverview() {
        SpankkiRequest getCardsRequest = new SpankkiRequest();
        addSessionIdRequestTokenAndDeviceId(
                getCardsRequest, SpankkiConstants.Url.CARDS_OVERVIEW.getRequestToken());

        return request(SpankkiConstants.Url.CARDS_OVERVIEW.getUrl())
                .post(String.class, getCardsRequest);
    }

    public LoanOverviewResponse fetchLoanOverview() {
        LoanOverviewRequest loanOverviewRequest =
                new LoanOverviewRequest().setCustomerId(this.sessionStorage.getCustomerId());
        addSessionIdRequestTokenAndDeviceId(
                loanOverviewRequest, SpankkiConstants.Url.LOAN_OVERVIEW.getRequestToken());

        return request(SpankkiConstants.Url.LOAN_OVERVIEW.getUrl())
                .post(LoanOverviewResponse.class, loanOverviewRequest);
    }

    public LoanDetailsResponse fetchLoanDetails(String loanId, String loanType) {
        LoanDetailsRequest loanDetailsRequest =
                new LoanDetailsRequest()
                        .setLoanDetails(
                                new LoanDetailsIdentity().setLoanId(loanId).setLoanType(loanType));

        addSessionIdRequestTokenAndDeviceId(
                loanDetailsRequest, SpankkiConstants.Url.LOAN_DETAILS.getRequestToken());

        return request(SpankkiConstants.Url.LOAN_DETAILS.getUrl())
                .post(LoanDetailsResponse.class, loanDetailsRequest);
    }

    public FundsPortfoliosResponse fetchFundsPortfolios() {
        SpankkiRequest fundsPortfoliosRequest = new SpankkiRequest();
        addSessionIdAndDeviceId(fundsPortfoliosRequest);

        return request(SpankkiConstants.Url.FUNDS_PORTFOLIOS.getUrl())
                .post(FundsPortfoliosResponse.class, fundsPortfoliosRequest);
    }

    public AllFundsResponse fetchAllFunds() {
        AllFundsRequest allFundsRequest = new AllFundsRequest();
        addSessionIdAndDeviceId(allFundsRequest);

        return request(SpankkiConstants.Url.GET_ALL_FUNDS.getUrl())
                .post(AllFundsResponse.class, allFundsRequest);
    }

    private RequestChallengeResponse requestChallenge(RequestChallengeRequest reqChallengeRequest) {

        return request(SpankkiConstants.Url.REQUEST_CHALLENGE.getUrl())
                .post(RequestChallengeResponse.class, reqChallengeRequest);
    }

    private SpankkiResponse respondToChallenge(RespondToChallengeRequest resChallengeRequest) {

        return request(SpankkiConstants.Url.RESPOND_TO_CHALLENGE.getUrl())
                .post(SpankkiResponse.class, resChallengeRequest);
    }

    private <T> T postLoginRequest(Class<T> c, URL requestUrl, SpankkiRequest requestData)
            throws AuthenticationException, AuthorizationException {
        SpankkiResponse response = (SpankkiResponse) request(requestUrl).post(c, requestData);

        SpankkiConstants.ServerResponse.throwIfError(response.getStatus());

        return (T) response;
    }

    private <T> T postRequest(Class<T> c, URL requestUrl, SpankkiRequest requestData) {
        SpankkiResponse response = (SpankkiResponse) request(requestUrl).post(c, requestData);

        if (!response.isOK()) {
            throw new IllegalStateException(response.getErrorMessage());
        }

        return (T) response;
    }

    private void addSessionIdAndRequestToken(SpankkiRequest request, String requestToken) {
        request.setSessionId(this.sessionStorage.getSessionId());
        request.setRequestToken(requestToken);
    }

    private void addSessionIdRequestTokenAndDeviceId(SpankkiRequest request, String requestToken) {
        addSessionIdAndRequestToken(request, requestToken);
        request.setDeviceId(this.persistentStorage.getDeviceId());
    }

    private void addSessionIdAndDeviceId(SpankkiRequest request) {
        request.setSessionId(this.sessionStorage.getSessionId());
        request.setDeviceId(this.persistentStorage.getDeviceId());
    }

    private String calculateRequestToken(String randomString) {
        String randomBytesString =
                randomString + SpankkiConstants.Authentication.REQUEST_TOKEN_RANDOM_STRING;

        return EncodingUtils.encodeAsBase64String(Hash.sha256(randomBytesString));
    }

    private String calculateChallengeResponse(String challenge) {
        String randomBytesString =
                challenge + SpankkiConstants.Authentication.CHALLENGE_RESPONSE_RANDOM_STRING;

        return EncodingUtils.encodeAsBase64String(Hash.sha256(randomBytesString));
    }

    private RequestBuilder request(URL url) {
        return this.client
                .request(url)
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
    }
}
