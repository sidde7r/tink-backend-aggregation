package se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordnet.session;

import com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordnet.NordnetApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordnet.NordnetConstants;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public final class NordnetSessionHandler implements SessionHandler {
    private final NordnetApiClient apiClient;
    private final SessionStorage sessionStorage;

    public NordnetSessionHandler(NordnetApiClient apiClient, SessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public void logout() {}

    @Override
    public void keepAlive() throws SessionException {
        if (Strings.isNullOrEmpty(sessionStorage.get(NordnetConstants.StorageKeys.SESSION_KEY))) {
            throw new SessionException(SessionError.SESSION_EXPIRED);
        }
    }
}
