package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.authenticator;

import com.google.common.base.Preconditions;
import java.util.UUID;
import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.RevolutApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.RevolutConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.authenticator.rpc.ConfirmSignInResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.authenticator.rpc.SignInResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.authenticator.rpc.UserExistResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.smsotp.SmsOtpAuthenticatorPassword;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class RevolutMultifactorAuthenticator implements SmsOtpAuthenticatorPassword<String> {

    private static final int RESEND_CODE_MAX_ATTEMPTS = 3;

    private final RevolutApiClient apiClient;
    private final PersistentStorage persistentStorage;

    public RevolutMultifactorAuthenticator(
            RevolutApiClient apiClient, PersistentStorage persistentStorage) {
        this.apiClient = apiClient;
        this.persistentStorage = persistentStorage;
    }

    @Override
    public String init(String username, String password)
            throws AuthenticationException, AuthorizationException {
        String deviceId = UUID.randomUUID().toString().toUpperCase();
        persistentStorage.put(RevolutConstants.Storage.DEVICE_ID, deviceId);

        UserExistResponse userExistResponse = apiClient.userExists(username);

        if (userExistResponse.isEmpty()) {
            throw LoginError.NOT_CUSTOMER.exception();
        }

        // Sometimes the registration options that we need to register the device is not available
        // immediately. Attempting to sign in again will make them available.
        for (int numAttempts = 0; numAttempts < RESEND_CODE_MAX_ATTEMPTS; numAttempts++) {

            if (tryRegisterDevice(username, password)) {
                return username;
            }
        }

        // If the user has tried to register the device within a short time-frame it is possible
        // that the registration options we need will not be available for several minutes.
        throw LoginError.REGISTER_DEVICE_ERROR.exception();
    }

    private boolean tryRegisterDevice(String username, String password) throws LoginException {

        // For a new device id this will cause Revolut to send an email that deep-links the
        // verification code to the Revolut app. For known device ids this will be an SMS instead.
        SignInResponse signInResponse = apiClient.signIn(username, password);

        // If SMS channel is used then everything is fine.
        if (signInResponse.isSmsChannel()) {
            return true;
        }

        // If we get a deep-link email we need to resend the code via voice call so that the user
        // may enter it manually.
        if (apiClient.getVerificationOptions(username).hasCallOption()) {

            apiClient.resendCodeViaCall(username);
            return true;
        }

        return false;
    }

    @Override
    public void authenticate(String otp, String initValues)
            throws AuthenticationException, AuthorizationException {

        ConfirmSignInResponse confirmSignInResponse;

        try {
            confirmSignInResponse = apiClient.confirmSignIn(initValues, otp);
        } catch (HttpResponseException e) {
            if (e.getResponse().getStatus() == HttpStatus.SC_UNPROCESSABLE_ENTITY) {
                throw LoginError.INCORRECT_CHALLENGE_RESPONSE.exception(e);
            }
            throw e;
        }

        String userId =
                Preconditions.checkNotNull(
                        confirmSignInResponse.getUser().getId(), "UserId can't be null");
        String accessToken =
                Preconditions.checkNotNull(
                        confirmSignInResponse.getAccessToken(), "Access token can't be null");

        persistentStorage.put(RevolutConstants.Storage.USER_ID, userId);
        persistentStorage.put(RevolutConstants.Storage.ACCESS_TOKEN, accessToken);
    }
}
