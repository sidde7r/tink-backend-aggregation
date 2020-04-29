package se.tink.backend.aggregation.agents.nxgen.se.business.nordea.session;

import com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.se.business.nordea.NordeaSEConstants.StorageKeys;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class NordeaSESessionHandler implements SessionHandler {
    private final SessionStorage sessionStorage;

    public NordeaSESessionHandler(SessionStorage sessionStorage) {
        this.sessionStorage = sessionStorage;
    }

    @Override
    public void logout() {}

    @Override
    public void keepAlive() throws SessionException {
        if (Strings.isNullOrEmpty(sessionStorage.get(StorageKeys.SECURITY_TOKEN))) {
            throw SessionError.SESSION_EXPIRED.exception();
        }
    }
}
