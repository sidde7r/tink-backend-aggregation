package se.tink.backend.aggregation.agents.nxgen.uk.revolut;

import com.google.api.client.http.HttpStatusCodes;
import com.google.common.base.Preconditions;
import se.tink.backend.aggregation.agents.nxgen.uk.revolut.authenticator.rpc.ConfirmSignInRequest;
import se.tink.backend.aggregation.agents.nxgen.uk.revolut.authenticator.rpc.ConfirmSignInResponse;
import se.tink.backend.aggregation.agents.nxgen.uk.revolut.authenticator.rpc.ResendCodeRequest;
import se.tink.backend.aggregation.agents.nxgen.uk.revolut.authenticator.rpc.SignInRequest;
import se.tink.backend.aggregation.agents.nxgen.uk.revolut.authenticator.rpc.UserExistResponse;
import se.tink.backend.aggregation.agents.nxgen.uk.revolut.entities.WalletEntity;
import se.tink.backend.aggregation.agents.nxgen.uk.revolut.fetcher.transactionalaccount.rpc.AccountsResponse;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class RevolutApiClient {
    private final TinkHttpClient client;
    private final PersistentStorage persistentStorage;

    public RevolutApiClient(TinkHttpClient client, PersistentStorage persistentStorage) {
        this.client = client;
        this.persistentStorage = persistentStorage;
    }

    public HttpResponse assertAuthorized() {
        return getUserAuthorizedRequest(RevolutConstants.Urls.FEATURES)
                .get(HttpResponse.class);
    }

    public UserExistResponse userExists(String username) {
        return getAppAuthorizedRequest(RevolutConstants.Urls.USER_EXIST)
                .queryParam("phones", username).get(UserExistResponse.class);
    }

    public void signIn(String username) {
        SignInRequest request = SignInRequest.build(username);

        HttpResponse response = getAppAuthorizedRequest(RevolutConstants.Urls.SIGN_IN)
                .post(HttpResponse.class, request);

        Preconditions.checkState(successfulRequest(response.getStatus()), "Init of sign in failed");
    }

    public void resendCodeViaCall(String username) {
        ResendCodeRequest request = new ResendCodeRequest();
        request.setPhone(username);

        HttpResponse response = getAppAuthorizedRequest(RevolutConstants.Urls.RESEND_CODE_VIA_CALL)
                .post(HttpResponse.class, request);

        Preconditions.checkState(successfulRequest(response.getStatus()), "Resend of code failed");
    }

    public ConfirmSignInResponse confirmSignIn(String phoneNumber, String verificationCode) {
        ConfirmSignInRequest request = ConfirmSignInRequest.build(phoneNumber, verificationCode);

        return getAppAuthorizedRequest(RevolutConstants.Urls.CONFIRM_SIGN_IN)
                .post(ConfirmSignInResponse.class, request);
    }

    public WalletEntity fetchWallet() {
        return getUserAuthorizedRequest(RevolutConstants.Urls.WALLET)
                .get(WalletEntity.class);
    }

    public AccountsResponse fetchAccounts() {
        return getUserAuthorizedRequest(RevolutConstants.Urls.TOPUP_ACCOUNTS)
                .get(AccountsResponse.class);
    }

    private RequestBuilder getAppAuthorizedRequest(URL url) {
        String authStringB64 = getFormattedAuthStringAsB64(
                RevolutConstants.AppAuthenticationValues.APP_AUTHORIZATION.getKey(),
                RevolutConstants.AppAuthenticationValues.APP_AUTHORIZATION.getValue());

        return client.request(url)
                .header(RevolutConstants.Headers.AUTHORIZATION_HEADER,
                        RevolutConstants.Headers.BASIC + authStringB64)
                .header(RevolutConstants.Headers.DEVICE_ID_HEADER,
                        persistentStorage.get(RevolutConstants.Storage.DEVICE_ID));
    }

    private RequestBuilder getUserAuthorizedRequest(URL url) {
        String clientId = persistentStorage.get(RevolutConstants.Storage.USER_ID);
        String accessToken = persistentStorage.get(RevolutConstants.Storage.ACCESS_TOKEN);
        String authStringB64 = getFormattedAuthStringAsB64(clientId, accessToken);

        return client.request(url)
                .header(RevolutConstants.Headers.AUTHORIZATION_HEADER,
                        RevolutConstants.Headers.BASIC + authStringB64)
                .header(RevolutConstants.Headers.DEVICE_ID_HEADER,
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
