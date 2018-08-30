package se.tink.backend.aggregation.agents.nxgen.fi.banks.alandsbanken.authenticator;

import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.AuthorizationError;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.alandsbanken.AlandsBankenApiClient;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.alandsbanken.AlandsBankenConstants;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.alandsbanken.AlandsBankenPersistentStorage;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.alandsbanken.UnexpectedFailureException;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.alandsbanken.authenticator.rpc.LoginWithTokenRequest;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.alandsbanken.authenticator.rpc.LoginWithTokenResponse;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticator;
import se.tink.backend.aggregation.rpc.Credentials;

public class AlandsBankenAutoAuthenticator implements PasswordAuthenticator, AutoAuthenticator {

    private static final AggregationLogger LOGGER = new AggregationLogger(AlandsBankenAutoAuthenticator.class);

    private final AlandsBankenApiClient client;
    private final AlandsBankenPersistentStorage persistentStorage;
    private final Credentials credentials;
    private final PasswordAuthenticationController authenticationController;

    public AlandsBankenAutoAuthenticator(
            AlandsBankenApiClient client, AlandsBankenPersistentStorage persistentStorage,
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
    public void authenticate(String username, String password) throws AuthenticationException, AuthorizationException {
        LoginWithTokenResponse response = client.loginWithToken(new LoginWithTokenRequest()
                .setDeviceToken(persistentStorage.getDeviceToken())
                .setDeviceId(persistentStorage.getDeviceId())
                .setPassword(password)
                .setAppVersion(AlandsBankenConstants.AutoAuthentication.APP_VERSION)
        );

        if (response.passwordExpired()) {
            persistentStorage.clearDeviceCredentials();
            throw AuthorizationError.ACCOUNT_BLOCKED.exception(
                    AlandsBankenConstants.EndUserMessage.PASSWORD_EXPIRED.getKey());
        }

        if (response.incorrectPassword()) {
            LOGGER.warn("The password was incorrect");
        } else if (response.isIncorrectDevice()) {
            persistentStorage.clearDeviceCredentials();
            LOGGER.warn("The token or device id was not recognized");
        }
        response.validate(() -> new UnexpectedFailureException(response, "Failure on auto authentication"));

        persistentStorage.persist(response);
    }
}
