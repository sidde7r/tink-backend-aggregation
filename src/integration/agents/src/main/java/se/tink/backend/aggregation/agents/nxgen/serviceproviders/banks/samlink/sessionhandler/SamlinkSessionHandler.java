package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.sessionhandler;

import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.SamlinkApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.SamlinkSessionStorage;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;

public class SamlinkSessionHandler implements SessionHandler {
    private final SamlinkApiClient apiClient;
    private final SamlinkSessionStorage sessionStorage;

    public SamlinkSessionHandler(SamlinkApiClient apiClient, SamlinkSessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public void logout() {}

    @Override
    public void keepAlive() throws SessionException {
        if (!sessionStorage.hasAccessToken()) {
            throw SessionError.SESSION_EXPIRED.exception();
        }
        try {
            apiClient.keepAlive();
        } catch (Exception e) {
            throw SessionError.SESSION_EXPIRED.exception();
        }
    }
}
