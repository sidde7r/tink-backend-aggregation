package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.session;

import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebCommonConstants;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public final class SEBSessionHandler implements SessionHandler {
    private final SebBaseApiClient apiClient;
    private final SessionStorage sessionStorage;

    public SEBSessionHandler(SebBaseApiClient apiClient, SessionStorage sessionStorage) {

        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public void logout() {}

    @Override
    public void keepAlive() throws SessionException {
        sessionStorage
                .get(SebCommonConstants.StorageKeys.TOKEN, OAuth2Token.class)
                .filter(t -> !t.hasAccessExpired())
                .orElseThrow(() -> SessionError.SESSION_EXPIRED.exception());
    }
}
