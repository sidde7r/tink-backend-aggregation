package se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea;

import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class NordeaDkSessionStorage {
    private static final String SESSION_ID = "session-id";
    private static final String TOKEN = "token";

    private SessionStorage sessionStorage;

    public NordeaDkSessionStorage(SessionStorage sessionStorage) {
        this.sessionStorage = sessionStorage;
    }

    public String getSessionId() {
        return sessionStorage.get(SESSION_ID);
    }

    public void setSessionId(String sessionId) {
        sessionStorage.put(SESSION_ID, sessionId);
    }

    public String getToken() {
        return sessionStorage.get(TOKEN);
    }

    public void setToken(String token) {
        sessionStorage.put(TOKEN, token);
    }
}
