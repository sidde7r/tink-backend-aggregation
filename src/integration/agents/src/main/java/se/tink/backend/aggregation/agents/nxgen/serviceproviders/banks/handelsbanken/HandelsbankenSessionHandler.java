package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken;

import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.rpc.KeepAliveResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.rpc.auto.AuthorizeResponse;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;

public class HandelsbankenSessionHandler implements SessionHandler {

    private static final AggregationLogger LOGGER =
            new AggregationLogger(HandelsbankenSessionHandler.class);

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

        LOGGER.info("in keep Alive: going to call HandelsbankenApiClient->keepAlive");

        KeepAliveResponse keepAlive =
                sessionStorage
                        .applicationEntryPoint()
                        .map(client::keepAlive)
                        .orElseThrow(HandelsbankenSessionHandler::sessionException);

        LOGGER.info(
                String.format(
                        "isAlive: %s, autostartToken:%s, errorMessage=%s",
                        keepAlive.isAlive(),
                        keepAlive.getAutoStartToken(),
                        keepAlive.createErrorMessage()));

        if (!keepAlive.isAlive()) {
            sessionStorage.removeApplicationEntryPoint();
            persistentStorage.removeAuthorizeResponse();
            LOGGER.info(
                    String.format(
                            "Session with Handelsbanken cannot be kept alive due to: %s",
                            keepAlive.createErrorMessage()));
            throw sessionException();
        }
    }

    private void validateUserIsLoggedIn() throws SessionException {
        AuthorizeResponse authorizeResponse =
                persistentStorage
                        .getAuthorizeResponse()
                        .orElseThrow(HandelsbankenSessionHandler::sessionException);
        if (authorizeResponse != null) {
            LOGGER.info(
                    String.format(
                            "validateUserIsLoggedIn: %s", authorizeResponse.getAutoStartToken()));
        }
    }

    private static SessionException sessionException() {
        return SessionError.SESSION_EXPIRED.exception();
    }
}
