package se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v1.authenticator;

import com.google.common.base.Strings;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v1.SpankkiApiClient;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v1.SpankkiConstants;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v1.SpankkiPersistentStorage;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v1.SpankkiSessionStorage;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v1.authenticator.rpc.TokenLoginResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v1.rpc.SpankkiResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.authenticator.AutoAuthenticator;

public class SpankkiAutoAuthenticator implements AutoAuthenticator {

    private final SpankkiApiClient apiClient;
    private final SpankkiPersistentStorage persistentStorage;
    private final SpankkiSessionStorage sessionStorage;
    private final Credentials credentials;

    public SpankkiAutoAuthenticator(
            SpankkiApiClient apiClient,
            SpankkiPersistentStorage persistentStorage,
            SpankkiSessionStorage sessionStorage,
            Credentials credentials) {
        this.apiClient = apiClient;
        this.persistentStorage = persistentStorage;
        this.sessionStorage = sessionStorage;
        this.credentials = credentials;
    }

    @Override
    public void autoAuthenticate() throws SessionException, BankServiceException {
        String username = this.credentials.getField(Field.Key.USERNAME);
        String password = this.credentials.getField(Field.Key.PASSWORD);
        String deviceId = this.persistentStorage.getDeviceId();
        String deviceToken = this.persistentStorage.getDeviceToken();

        credentials.setSensitivePayload(Field.Key.USERNAME, username);
        credentials.setSensitivePayload(Field.Key.PASSWORD, password);
        credentials.setSensitivePayload(Field.Key.ACCESS_TOKEN, deviceToken);

        if (Strings.isNullOrEmpty(username)
                || Strings.isNullOrEmpty(password)
                || Strings.isNullOrEmpty(deviceId)
                || Strings.isNullOrEmpty(deviceToken)) {
            throw SessionError.SESSION_EXPIRED.exception();
        }

        try {
            SpankkiResponse challengeResponse = this.apiClient.handleSetupChallenge();
            this.sessionStorage.putSessionId(challengeResponse.getSessionId());
            TokenLoginResponse loginResponse =
                    this.apiClient.loginWithToken(password, deviceId, deviceToken);
            if (loginResponse.isMustChangePassword()) {
                throw LoginError.INCORRECT_CREDENTIALS.exception(
                        SpankkiConstants.Authentication.PASSWORD_CHANGE_MSG);
            }

            deviceToken = loginResponse.getNewToken();
            // store customer id in session
            sessionStorage.putCustomerId(loginResponse.getCustomer().getCustomerId());
            sessionStorage.putCustomerEntity(loginResponse.getCustomer());
        } catch (BankServiceException e) {
            throw e;
        } catch (AuthenticationException | AuthorizationException e) {
            throw SessionError.SESSION_EXPIRED.exception(e);
        }

        // Update device token
        this.persistentStorage.putDeviceToken(deviceToken);
    }
}
