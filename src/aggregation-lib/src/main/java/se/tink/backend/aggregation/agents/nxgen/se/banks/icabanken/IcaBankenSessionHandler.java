package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken;

import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class IcaBankenSessionHandler implements SessionHandler {

    private final IcaBankenApiClient apiClient;
    private final SessionStorage sessionStorage;

    public IcaBankenSessionHandler(IcaBankenApiClient apiClient, SessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public void logout() {}

    @Override
    public void keepAlive() throws SessionException {
        if ((sessionStorage.get(IcaBankenConstants.IdTags.SESSION_ID_TAG)!=null)){
            apiClient.keepAlive();
            return;
        }
        throw SessionError.SESSION_EXPIRED.exception();
    }
}
