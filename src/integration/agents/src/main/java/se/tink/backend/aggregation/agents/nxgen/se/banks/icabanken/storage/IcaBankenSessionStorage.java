package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.storage;

import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.IcaBankenConstants;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class IcaBankenSessionStorage {
    private SessionStorage sessionStorage;

    public IcaBankenSessionStorage(SessionStorage sessionStorage) {
        this.sessionStorage = sessionStorage;
    }

    public void saveSessionId(String sessionId) {
        sessionStorage.put(IcaBankenConstants.IdTags.SESSION_ID_TAG, sessionId);
    }

    public String getSessionId() {
        return sessionStorage.get(IcaBankenConstants.IdTags.SESSION_ID_TAG);
    }
}
