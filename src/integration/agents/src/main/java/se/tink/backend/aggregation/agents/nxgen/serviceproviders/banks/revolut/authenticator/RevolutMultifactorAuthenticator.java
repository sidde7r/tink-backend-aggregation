package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.authenticator;

import com.google.common.base.Preconditions;
import java.util.UUID;
import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.RevolutApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.RevolutConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.authenticator.rpc.ConfirmSignInResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.authenticator.rpc.UserExistResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.smsotp.SmsOtpAuthenticatorPassword;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class RevolutMultifactorAuthenticator implements SmsOtpAuthenticatorPassword<String> {
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

        // This request will trigger a verification code being sent to the user's phone via text
        try {
            apiClient.signIn(username, password);
        } catch (HttpResponseException e) {
            if (e.getResponse().getStatus() == HttpStatus.SC_UNAUTHORIZED) {
                throw LoginError.INCORRECT_CREDENTIALS.exception();
            }
            throw e;
        }

        return username;
    }

    @Override
    public void authenticate(String otp, String initValues)
            throws AuthenticationException, AuthorizationException {

        ConfirmSignInResponse confirmSignInResponse;

        try {
            confirmSignInResponse = apiClient.confirmSignIn(initValues, otp);
        } catch (HttpResponseException e) {
            if (e.getResponse().getStatus() == HttpStatus.SC_UNPROCESSABLE_ENTITY) {
                throw LoginError.INCORRECT_CHALLENGE_RESPONSE.exception();
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
