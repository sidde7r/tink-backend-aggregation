package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.authenticator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.CrossKeyApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.CrossKeyConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.CrossKeyPersistentStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.UnexpectedFailureException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.authenticator.rpc.LoginWithTokenRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.authenticator.rpc.LoginWithTokenResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticator;

public class CrossKeyAutoAuthenticator implements PasswordAuthenticator, AutoAuthenticator {

    private static final Logger LOG = LoggerFactory.getLogger(CrossKeyAutoAuthenticator.class);

    private final CrossKeyApiClient client;
    private final CrossKeyPersistentStorage persistentStorage;
    private final Credentials credentials;
    private final PasswordAuthenticationController authenticationController;

    public CrossKeyAutoAuthenticator(
            CrossKeyApiClient client,
            CrossKeyPersistentStorage persistentStorage,
            Credentials credentials) {
        this.client = client;
        this.persistentStorage = persistentStorage;
        this.credentials = credentials;
        authenticationController = new PasswordAuthenticationController(this);
    }

    @Override
    public void autoAuthenticate() throws SessionException, AuthorizationException {
        if (!persistentStorage.readyForSingleFactor()) {
            throw SessionError.SESSION_EXPIRED.exception();
        }
        try {
            authenticationController.authenticate(credentials);
        } catch (AuthenticationException e) {
            throw SessionError.SESSION_EXPIRED.exception();
        }
    }

    @Override
    public void authenticate(String username, String password)
            throws AuthenticationException, AuthorizationException {
        LoginWithTokenResponse response =
                client.loginWithToken(
                        new LoginWithTokenRequest()
                                .setDeviceToken(persistentStorage.getDeviceToken())
                                .setDeviceId(persistentStorage.getDeviceId())
                                .setPassword(password)
                                .setAppVersion(CrossKeyConstants.AutoAuthentication.APP_VERSION));

        if (response.passwordExpired()) {
            persistentStorage.clearDeviceCredentials();
            throw LoginError.INCORRECT_CREDENTIALS.exception(
                    CrossKeyConstants.EndUserMessage.PASSWORD_EXPIRED.getKey());
        }

        if (response.incorrectPassword()) {
            persistentStorage.clearDeviceCredentials();
            LOG.warn("The password was incorrect");
            throw SessionError.SESSION_EXPIRED.exception();
        } else if (response.isIncorrectDevice()) {
            persistentStorage.clearDeviceCredentials();
            LOG.warn("The token or device id was not recognized");
            throw SessionError.SESSION_EXPIRED.exception();
        }

        response.validate(
                () -> new UnexpectedFailureException(response, "Failure on auto authentication"));

        persistentStorage.persistDeviceToken(response);
    }
}
