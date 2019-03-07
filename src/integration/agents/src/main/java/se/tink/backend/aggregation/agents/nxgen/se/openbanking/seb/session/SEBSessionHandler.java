package se.tink.backend.aggregation.agents.nxgen.se.openbanking.seb.session;

import org.assertj.core.util.Strings;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.seb.SEBApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.seb.SEBConstants;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public final class SEBSessionHandler implements SessionHandler {
    private final SEBApiClient apiClient;
    private final SessionStorage sessionStorage;

    public SEBSessionHandler(SEBApiClient apiClient, SessionStorage sessionStorage) {

        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public void logout() {

    }

    @Override
    public void keepAlive() throws SessionException {
        if (Strings.isNullOrEmpty(this.sessionStorage.get(SEBConstants.STORAGE.TOKEN)))
            throw SessionError.SESSION_EXPIRED.exception();
    }
}
