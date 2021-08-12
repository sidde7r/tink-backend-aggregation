package se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.session;

import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.SBABApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.SBABConstants.StorageKeys;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class SBABSessionHandler implements SessionHandler {
    private final SBABApiClient apiClient;
    private final SessionStorage sessionStorage;

    public SBABSessionHandler(SBABApiClient apiClient, SessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public void logout() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void keepAlive() throws SessionException {
        if (sessionStorage.containsKey(StorageKeys.BEARER_TOKEN)
                && sessionStorage.containsKey(StorageKeys.CONTACT_INFO_ENDPOINT)) {
            // check if can fetch contact info
            try {
                apiClient.fetchContactInfo();
            } catch (HttpResponseException e) {
                throw SessionError.SESSION_EXPIRED.exception();
            }
        }
        throw SessionError.SESSION_EXPIRED.exception();
    }
}
