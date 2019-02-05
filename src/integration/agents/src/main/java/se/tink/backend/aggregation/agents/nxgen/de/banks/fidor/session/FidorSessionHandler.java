package se.tink.backend.aggregation.agents.nxgen.de.banks.fidor.session;

import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fidor.FidorApiClient;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;

public class FidorSessionHandler implements SessionHandler {

    private final FidorApiClient fidorApiClient;

    public FidorSessionHandler(FidorApiClient fidorApiClient){
        this.fidorApiClient = fidorApiClient;
    }

    @Override
    public void logout() {
        this.fidorApiClient.clearPersistentStorage();
    }

    @Override
    public void keepAlive() throws SessionException {
        if(!this.fidorApiClient.isSessionAlive()){
            throw SessionError.SESSION_EXPIRED.exception();
        }
    }
}
