package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken;

import java.lang.invoke.MethodHandles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.rpc.KeepAliveResponse;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;

public class HandelsbankenSessionHandler implements SessionHandler {
    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final HandelsbankenApiClient client;
    private final HandelsbankenPersistentStorage persistentStorage;
    private final Credentials credentials;
    private final HandelsbankenSessionStorage sessionStorage;

    public HandelsbankenSessionHandler(
            HandelsbankenApiClient client,
            HandelsbankenPersistentStorage persistentStorage,
            Credentials credentials,
            HandelsbankenSessionStorage sessionStorage) {
        this.client = client;
        this.persistentStorage = persistentStorage;
        this.credentials = credentials;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public void logout() {
        persistentStorage.getAuthorizeResponse().ifPresent(client::applicationExitPoint);
        persistentStorage.removeAuthorizeResponse();
    }

    @Override
    public void keepAlive() throws SessionException {
        validateUserIsLoggedIn();

        KeepAliveResponse keepAlive =
                sessionStorage
                        .applicationEntryPoint()
                        .map(client::keepAlive)
                        .orElseThrow(HandelsbankenSessionHandler::sessionException);

        if (!keepAlive.isAlive()) {
            sessionStorage.removeApplicationEntryPoint();
            persistentStorage.removeAuthorizeResponse();
            logger.info(
                    String.format(
                            "Session with Handelsbanken cannot be kept alive due to: %s",
                            keepAlive.createErrorMessage()));
            throw sessionException();
        }
    }

    private void validateUserIsLoggedIn() throws SessionException {
        persistentStorage
                .getAuthorizeResponse()
                .orElseThrow(HandelsbankenSessionHandler::sessionException);
    }

    private static SessionException sessionException() {
        return SessionError.SESSION_EXPIRED.exception();
    }
}
