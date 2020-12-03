package se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken;

import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

public class HandelsbankenNOSessionHandler implements SessionHandler {
    private final HandelsbankenNOApiClient apiClient;

    HandelsbankenNOSessionHandler(HandelsbankenNOApiClient apiClient) {
        this.apiClient = apiClient;
    }

    // TODO implement logout action
    @Override
    public void logout() {
        // no operation
    }

    @Override
    public void keepAlive() throws SessionException {
        try {
            apiClient.fetchUserSettings();
        } catch (HttpResponseException e) {
            throw SessionError.SESSION_EXPIRED.exception(e);
        }
    }
}
