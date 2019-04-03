package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbankenbase.session;

import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbankenbase.HandelsbankenBaseConstants.StorageKeys;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class HandelsbankenBaseSessionHandler implements SessionHandler {

    private final SessionStorage sessionStorage;

    public HandelsbankenBaseSessionHandler(SessionStorage sessionStorage) {
        this.sessionStorage = sessionStorage;
    }

    @Override
    public void logout() {

    }

    @Override
    public void keepAlive() throws SessionException {
        String accessToken = sessionStorage.get(StorageKeys.ACCESS_TOKEN);
        if (accessToken == null) {
            throw SessionError.SESSION_EXPIRED.exception();
        }
    }
}
