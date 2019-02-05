package se.tink.backend.aggregation.agents.nxgen.be.banks.argenta;

import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class ArgentaSessionStorage {
    private SessionStorage sessionStorage;

    public ArgentaSessionStorage(SessionStorage sessionStorage) {
        this.sessionStorage = sessionStorage;
    }

    public String getAuthorization() {
        return sessionStorage.get(ArgentaConstants.HEADER.AUTHORIZATION);
    }

    public void setAuthorization(String authorizationBearer) {
        sessionStorage.put(ArgentaConstants.HEADER.AUTHORIZATION, authorizationBearer);
    }
}
