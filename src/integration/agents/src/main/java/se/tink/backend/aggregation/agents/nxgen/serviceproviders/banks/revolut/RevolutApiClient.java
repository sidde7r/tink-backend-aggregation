package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut;

import com.google.api.client.http.HttpStatusCodes;
import com.google.common.base.Preconditions;
import java.util.List;
import java.util.stream.Collectors;
import javax.ws.rs.core.MediaType;
import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.RevolutConstants.Params;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.RevolutConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.authenticator.rpc.ConfirmSignInRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.authenticator.rpc.ConfirmSignInResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.authenticator.rpc.ResendCodeRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.authenticator.rpc.SignInRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.authenticator.rpc.SignInResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.authenticator.rpc.UserExistResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.authenticator.rpc.VerificationOptionsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.fetcher.investment.rpc.InvestmentAccountResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.fetcher.investment.rpc.StockInfoResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.fetcher.investment.rpc.StockPriceResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.fetcher.transactionalaccount.rpc.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.fetcher.transactionalaccount.rpc.TransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.fetcher.transactionalaccount.rpc.WalletResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.rpc.BaseUserResponse;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.encoding.EncodingUtils;

public class RevolutApiClient {
    private final TinkHttpClient client;
    private final PersistentStorage persistentStorage;
    private RevolutConfiguration configuration;

    public RevolutApiClient(
            TinkHttpClient client,
            PersistentStorage persistentStorage,
            RevolutConfiguration configuration) {
        this.client = client;
        this.persistentStorage = persistentStorage;
        this.configuration = configuration;
    }

    public HttpResponse assertAuthorized() {
        return getUserAuthorizedRequest(RevolutConstants.Urls.FEATURES).get(HttpResponse.class);
    }

    public UserExistResponse userExists(String username) {
        return getAppAuthorizedRequest(RevolutConstants.Urls.USER_EXIST)
                .queryParam(RevolutConstants.Params.PHONES, username)
                .get(UserExistResponse.class);
    }

    public SignInResponse signIn(String username, String password) throws LoginException {

        try {

            SignInRequest request = SignInRequest.build(username, password);
            return getAppAuthorizedPostRequest(RevolutConstants.Urls.SIGN_IN)
                    .post(SignInResponse.class, request);

        } catch (HttpResponseException e) {
            if (e.getResponse().getStatus() == HttpStatus.SC_UNAUTHORIZED) {
                throw LoginError.INCORRECT_CREDENTIALS.exception(e);
            }
            throw e;
        }
    }

    public VerificationOptionsResponse getVerificationOptions(String username) {
        return getAppAuthorizedRequest(Urls.VERIFICATION_OPTIONS)
                .queryParam(Params.PHONE, username)
                .get(VerificationOptionsResponse.class);
    }

    public void resendCodeViaCall(String username) {
        ResendCodeRequest request = new ResendCodeRequest();
        request.setPhone(username);

        HttpResponse response =
                getAppAuthorizedPostRequest(RevolutConstants.Urls.RESEND_CODE_VIA_CALL)
                        .post(HttpResponse.class, request);

        Preconditions.checkState(successfulRequest(response.getStatus()), "Resend of code failed");
    }

    public ConfirmSignInResponse confirmSignIn(String phoneNumber, String verificationCode) {
        ConfirmSignInRequest request = ConfirmSignInRequest.build(phoneNumber, verificationCode);

        return getAppAuthorizedPostRequest(RevolutConstants.Urls.CONFIRM_SIGN_IN)
                .post(ConfirmSignInResponse.class, request);
    }

    public BaseUserResponse fetchUser() {
        return getUserAuthorizedRequest(RevolutConstants.Urls.USER_CURRENT)
                .get(BaseUserResponse.class);
    }

    public AccountsResponse fetchAccounts() {
        return getUserAuthorizedRequest(RevolutConstants.Urls.TOPUP_ACCOUNTS)
                .get(AccountsResponse.class);
    }

    public WalletResponse fetchWallet() {
        return getUserAuthorizedRequest(RevolutConstants.Urls.WALLET).get(WalletResponse.class);
    }

    public TransactionsResponse fetchTransactions(int count, String toDateMillis) {
        return getUserAuthorizedRequest(RevolutConstants.Urls.TRANSACTIONS)
                .queryParam(RevolutConstants.Params.COUNT, Integer.toString(count))
                .queryParam(RevolutConstants.Params.TO, toDateMillis)
                .get(TransactionsResponse.class);
    }

    public InvestmentAccountResponse fetchInvestmentAccounts() {
        return getUserAuthorizedRequest(RevolutConstants.Urls.PORTFOLIO)
                .get(InvestmentAccountResponse.class);
    }

    public StockInfoResponse fetchStockInfo() {
        return getUserAuthorizedRequest(RevolutConstants.Urls.STOCK_INFO)
                .get(StockInfoResponse.class);
    }

    public StockPriceResponse fetchCurrentStockPrice(List<String> stocks) {
        String request =
                stocks.stream()
                        .map(s -> String.format("\"%s\"", s))
                        .collect(Collectors.toList())
                        .toString();

        return getUserAuthorizedRequest(RevolutConstants.Urls.STOCK_PRICE_OVERVIEW)
                .post(StockPriceResponse.class, request);
    }

    private RequestBuilder getAppAuthorizedRequest(URL url) {
        String authStringB64 =
                getFormattedAuthStringAsB64("App", configuration.getAppAuthorization());

        persistentStorage.put(
                RevolutConstants.Headers.AUTHORIZATION_HEADER,
                RevolutConstants.Headers.BASIC + authStringB64);
        return client.request(url)
                .header(
                        RevolutConstants.Headers.AUTHORIZATION_HEADER,
                        RevolutConstants.Headers.BASIC + authStringB64)
                .header(
                        RevolutConstants.Headers.DEVICE_ID_HEADER,
                        persistentStorage.get(RevolutConstants.Storage.DEVICE_ID));
    }

    private RequestBuilder getAppAuthorizedPostRequest(URL url) {
        return getAppAuthorizedRequest(url).type(MediaType.APPLICATION_JSON_TYPE);
    }

    private RequestBuilder getUserAuthorizedRequest(URL url) {
        String clientId = persistentStorage.get(RevolutConstants.Storage.USER_ID);
        String accessToken = persistentStorage.get(RevolutConstants.Storage.ACCESS_TOKEN);
        String authStringB64 = getFormattedAuthStringAsB64(clientId, accessToken);

        return client.request(url)
                .header(
                        RevolutConstants.Headers.AUTHORIZATION_HEADER,
                        RevolutConstants.Headers.BASIC + authStringB64)
                .header(
                        RevolutConstants.Headers.DEVICE_ID_HEADER,
                        persistentStorage.get(RevolutConstants.Storage.DEVICE_ID));
    }

    private boolean successfulRequest(int status) {
        return status == HttpStatusCodes.STATUS_CODE_NO_CONTENT
                || status == HttpStatusCodes.STATUS_CODE_OK;
    }

    private String getFormattedAuthStringAsB64(String userId, String password) {
        String authString = userId + ":" + password;
        return EncodingUtils.encodeAsBase64String(authString.getBytes());
    }
}
