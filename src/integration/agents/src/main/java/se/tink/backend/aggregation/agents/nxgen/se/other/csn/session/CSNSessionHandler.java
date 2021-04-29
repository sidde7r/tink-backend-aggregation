package se.tink.backend.aggregation.agents.nxgen.se.other.csn.session;

import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.se.other.csn.CSNApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.other.csn.CSNAuthSessionStorageHelper;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

@RequiredArgsConstructor
public class CSNSessionHandler implements SessionHandler {

    private final CSNApiClient apiClient;
    private final CSNAuthSessionStorageHelper authSessionStorageHelper;

    @Override
    public void logout() {
        // Not implemented
    }

    @Override
    public void keepAlive() throws SessionException {
        if (!authSessionStorageHelper.getAccessToken().isPresent()) {
            throw SessionError.SESSION_EXPIRED.exception();
        }

        try {
            // Try to fetch user info to verify that the access token is still valid.
            apiClient.fetchUserInfo();
        } catch (HttpResponseException e) {
            throw SessionError.SESSION_EXPIRED.exception(e);
        }
    }
}
