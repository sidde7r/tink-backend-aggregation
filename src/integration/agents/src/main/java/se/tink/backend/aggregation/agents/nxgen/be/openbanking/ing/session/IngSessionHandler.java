package se.tink.backend.aggregation.agents.nxgen.be.openbanking.ing.session;

import org.assertj.core.util.Strings;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.ing.IngApiClient;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.ing.IngConstants;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public final class IngSessionHandler implements SessionHandler {

    private final IngApiClient apiClient;
    private final SessionStorage sessionStorage;

    public IngSessionHandler(IngApiClient apiClient, SessionStorage sessionStorage) {

        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public void logout() {
    }

    @Override
    public void keepAlive() throws SessionException {
        if (Strings.isNullOrEmpty(this.sessionStorage.get(IngConstants.StorageKeys.TOKEN))) {
            throw SessionError.SESSION_EXPIRED.exception();
        }
    }
}
