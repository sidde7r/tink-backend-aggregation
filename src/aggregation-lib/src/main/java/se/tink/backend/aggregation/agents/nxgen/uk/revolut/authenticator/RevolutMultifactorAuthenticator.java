package se.tink.backend.aggregation.agents.nxgen.uk.revolut.authenticator;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.Uninterruptibles;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.uk.revolut.RevolutApiClient;
import se.tink.backend.aggregation.agents.nxgen.uk.revolut.RevolutConstants;
import se.tink.backend.aggregation.agents.nxgen.uk.revolut.authenticator.rpc.ConfirmSignInResponse;
import se.tink.backend.aggregation.agents.nxgen.uk.revolut.authenticator.rpc.UserExistResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.smsotp.SmsOtpAuthenticator;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class RevolutMultifactorAuthenticator implements SmsOtpAuthenticator<String> {
    private final RevolutApiClient apiClient;
    private final PersistentStorage persistentStorage;

    public RevolutMultifactorAuthenticator(RevolutApiClient apiClient, PersistentStorage persistentStorage) {
        this.apiClient = apiClient;
        this.persistentStorage = persistentStorage;
    }

    @Override
    public String init(String username) throws AuthenticationException, AuthorizationException {
        String deviceId = UUID.randomUUID().toString().toUpperCase();
        persistentStorage.put(RevolutConstants.Storage.DEVICE_ID, deviceId);

        UserExistResponse userExistResponse = apiClient.userExists(username);

        if (userExistResponse.isEmpty()) {
            throw LoginError.NOT_CUSTOMER.exception();
        }

        // This request will trigger a verification code being sent to the user's phone via text
        apiClient.signIn(username);

        // ------------------------------------------------------------------------------------------
        // Resending the code via a phonecall after 30 seconds for testing purposes as the text doesn't reach
        // David's phone. Remove when flow is confirmed, we will only support getting a code via text.
        Uninterruptibles.sleepUninterruptibly(30, TimeUnit.SECONDS);
        apiClient.resendCodeViaCall(username);
        // ------------------------------------------------------------------------------------------

        return username;
    }

    @Override
    public void authenticate(String otp, String initValues) throws AuthenticationException, AuthorizationException {
        ConfirmSignInResponse confirmSignInResponse = apiClient.confirmSignIn(initValues, otp);

        String userId = Preconditions.checkNotNull(confirmSignInResponse.getUser().getId(),
                "UserId can't be null");
        String accessToken = Preconditions.checkNotNull(confirmSignInResponse.getAccessToken(),
                "Access token can't be null");

        persistentStorage.put(RevolutConstants.Storage.USER_ID, userId);
        persistentStorage.put(RevolutConstants.Storage.ACCESS_TOKEN, accessToken);
    }
}
