package se.tink.backend.aggregation.agents.nxgen.fi.banks.alandsbanken.sessionhandler;

import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.alandsbanken.AlandsBankenApiClient;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.alandsbanken.sessionhandler.rpc.KeepAliveResponse;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;

public class AlandsBankenSessionHandler implements SessionHandler {

    private final AlandsBankenApiClient client;

    public AlandsBankenSessionHandler(AlandsBankenApiClient client) {
        this.client = client;
    }

    @Override
    public void logout() {
        this.client.logout();
    }

    @Override
    public void keepAlive() throws SessionException {
        KeepAliveResponse response = this.client.keepAlive();
        if (response.isFailure()) {
            throw SessionError.SESSION_EXPIRED.exception();
        }
    }
}
