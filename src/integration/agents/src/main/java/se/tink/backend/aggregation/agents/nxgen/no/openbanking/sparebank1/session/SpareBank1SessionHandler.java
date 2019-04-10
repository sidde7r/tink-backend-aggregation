package se.tink.backend.aggregation.agents.nxgen.no.openbanking.sparebank1.session;

import com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.sparebank1.SpareBank1ApiClient;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.sparebank1.SpareBank1Constants;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public final class SpareBank1SessionHandler implements SessionHandler {
    private final SpareBank1ApiClient apiClient;
    private final SessionStorage sessionStorage;

    public SpareBank1SessionHandler(SpareBank1ApiClient apiClient, SessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public void logout() {}

    @Override
    public void keepAlive() throws SessionException {
        if (Strings.isNullOrEmpty(this.sessionStorage.get(SpareBank1Constants.StorageKeys.TOKEN))) {
            throw SessionError.SESSION_EXPIRED.exception();
        }
    }
}
