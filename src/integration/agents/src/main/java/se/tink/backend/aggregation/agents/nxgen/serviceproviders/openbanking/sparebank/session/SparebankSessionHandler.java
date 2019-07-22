package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.session;

import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.SparebankApiClient;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.exceptions.NotImplementedException;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public final class SparebankSessionHandler implements SessionHandler {

    private final SparebankApiClient apiClient;
    private final PersistentStorage persistentStorage;

    public SparebankSessionHandler(
            SparebankApiClient apiClient, PersistentStorage persistentStorage) {
        this.apiClient = apiClient;
        this.persistentStorage = persistentStorage;
    }

    @Override
    public void logout() {}

    @Override
    public void keepAlive() throws SessionException {
        try {
            throw new NotImplementedException("keepAlive not implemented");
        } catch (Exception e) {
            throw new SessionException(SessionError.SESSION_EXPIRED);
        }
    }
}
