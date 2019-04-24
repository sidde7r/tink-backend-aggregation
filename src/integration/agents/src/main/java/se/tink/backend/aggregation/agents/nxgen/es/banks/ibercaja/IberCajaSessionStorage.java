package se.tink.backend.aggregation.agents.nxgen.es.banks.ibercaja;

import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class IberCajaSessionStorage {

    private final SessionStorage sessionStorage;

    public IberCajaSessionStorage(SessionStorage sessionStorage) {

        this.sessionStorage = sessionStorage;
    }

    public void saveLoginResponse(String accessToken, String refreshToken) {
        sessionStorage.put(IberCajaConstants.Storage.ACCESS_TOKEN, accessToken);
        sessionStorage.put(IberCajaConstants.Storage.REFRESH_TOKEN, refreshToken);
    }

    public void saveUsername(String username) {
        sessionStorage.put(IberCajaConstants.Storage.USERNAME, username);
    }

    public void saveTicket(String ticket) {
        sessionStorage.put(IberCajaConstants.Storage.TICKET, ticket);
    }

    public String getUsername() {
        return sessionStorage.get(IberCajaConstants.Storage.USERNAME);
    }

    public String getTicket() {
        return sessionStorage.get(IberCajaConstants.Storage.TICKET);
    }
}
