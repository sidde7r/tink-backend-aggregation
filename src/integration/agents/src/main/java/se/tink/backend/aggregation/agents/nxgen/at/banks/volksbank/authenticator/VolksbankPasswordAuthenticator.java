package se.tink.backend.aggregation.agents.nxgen.at.banks.volksbank.authenticator;

import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.at.banks.volksbank.VolksbankApiClient;
import se.tink.backend.aggregation.agents.nxgen.at.banks.volksbank.VolksbankConstants;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.MultiFactorAuthenticator;

public class VolksbankPasswordAuthenticator implements MultiFactorAuthenticator {

    private final VolksbankApiClient apiClient;

    public VolksbankPasswordAuthenticator(VolksbankApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public void authenticate(Credentials credentials)
            throws AuthenticationException, AuthorizationException {
        String userId = credentials.getField(VolksbankConstants.CREDENTIAL_USERNUMBER);
        String userName = credentials.getField(Field.Key.USERNAME);
        String password = credentials.getField(Field.Key.PASSWORD);

        apiClient.getLogin();
        apiClient.postExtensionsOtpLogin();
        apiClient.postExtensions();
        apiClient.getLoginKeepSession();
        apiClient.postLoginOtp();
        String response = apiClient.postLoginUserNamePassword(userId, userName, password);
        if (response.toLowerCase().contains(VolksbankConstants.Errors.INVALID_ACCESS)) {
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }
        apiClient.getGenerateBinding();
        apiClient.postGenerateBindingQuickIdAction();
        apiClient.postGenerateBindingTouchIdAction();
        apiClient.postGenerateBindingFinalAction();
        apiClient.postMobileDevices();
        apiClient.getMain();
    }

    @Override
    public CredentialsTypes getType() {
        return CredentialsTypes.PASSWORD;
    }
}
