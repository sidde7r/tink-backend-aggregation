package se.tink.backend.aggregation.agents.nxgen.es.banks.ibercaja;

import com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ibercaja.authenticator.rpc.SessionResponse;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class IberCajaSessionStorage {

    private final SessionStorage sessionStorage;

    public IberCajaSessionStorage(SessionStorage sessionStorage) {

        this.sessionStorage = sessionStorage;
    }

    public void saveUsername(String username) {
        sessionStorage.put(IberCajaConstants.Storage.USERNAME, username);
    }

    public String getUsername() {
        return sessionStorage.get(IberCajaConstants.Storage.USERNAME);
    }

    public String getTicket() {
        return sessionStorage.get(IberCajaConstants.Storage.TICKET);
    }

    public String getFullName() {
        return sessionStorage.get(IberCajaConstants.Storage.FULL_NAME);
    }

    public String getDocumentNumber() {
        return sessionStorage.get(IberCajaConstants.Storage.DOCUMENT_NUMBER);
    }

    public void saveNici(String nici) {
        sessionStorage.put(IberCajaConstants.Storage.NICI, nici);
    }

    public String getNici() {
        return sessionStorage.get(IberCajaConstants.Storage.NICI);
    }

    public void saveInitSessionResponse(SessionResponse sessionResponse) {
        sessionStorage.put(IberCajaConstants.Storage.TICKET, sessionResponse.getTicket());
        sessionStorage.put(IberCajaConstants.Storage.FULL_NAME, sessionResponse.getName());

        if (!Strings.isNullOrEmpty(sessionResponse.getNif())) {
            sessionStorage.put(IberCajaConstants.Storage.DOCUMENT_NUMBER, sessionResponse.getNif());
        }
        // store NICI in the session storage to be masked in the logs
        // remove this if the NICI should not be considered as sensitive ifo
        sessionStorage.put(IberCajaConstants.Storage.NICI, sessionResponse.getNici());
        sessionStorage.put(
                IberCajaConstants.Storage.TOKEN_IDENTITY, sessionResponse.getTokenIdentity());

        sessionStorage.put(
                IberCajaConstants.Storage.CONTRACT, sessionResponse.getContractInCourse());
        sessionStorage.put(IberCajaConstants.Storage.NIP, sessionResponse.getNip());
    }

    public String getContractInCourse() {
        return sessionStorage.get(IberCajaConstants.Storage.CONTRACT);
    }

    public String getNip() {
        return sessionStorage.get(IberCajaConstants.Storage.NIP);
    }

    public String getTokenIdentity() {
        return sessionStorage.get(IberCajaConstants.Storage.TOKEN_IDENTITY);
    }
}
