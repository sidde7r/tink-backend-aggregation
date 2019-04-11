package se.tink.backend.aggregation.agents.nxgen.se.openbanking.seb.session;

import org.assertj.core.util.Strings;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.seb.SebApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.seb.SebConstants;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public final class SebSessionHandler implements SessionHandler {

    private final SebApiClient apiClient;
    private final SessionStorage sessionStorage;

    public SebSessionHandler(SebApiClient apiClient, SessionStorage sessionStorage) {

        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public void logout() {}

    @Override
    public void keepAlive() throws SessionException {
        if (Strings.isNullOrEmpty(this.sessionStorage.get(SebConstants.StorageKeys.TOKEN)))
            throw SessionError.SESSION_EXPIRED.exception();
    }
}
