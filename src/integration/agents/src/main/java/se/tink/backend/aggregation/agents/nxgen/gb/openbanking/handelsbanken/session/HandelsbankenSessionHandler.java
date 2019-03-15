package se.tink.backend.aggregation.agents.nxgen.gb.openbanking.handelsbanken.session;

import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.gb.openbanking.handelsbanken.HandelsbankenApiClient;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.exceptions.NotImplementedException;

public final class HandelsbankenSessionHandler implements SessionHandler {
    private final HandelsbankenApiClient apiClient;

    public HandelsbankenSessionHandler(HandelsbankenApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public void logout() {

    }

    @Override
    public void keepAlive() throws SessionException {
        try {
            throw new NotImplementedException("keepAlive not implemented");
        } catch (Exception e) {
            throw new SessionException(SessionError.SESSION_EXPIRED);
        }
    }
}
