package se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.session;

import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.HVBStorage;
import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.worklight.WLApiClient;
import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.worklight.WLConfig;
import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.worklight.session.WLSessionHandler;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;

public final class HVBSessionHandler implements SessionHandler {
    private WLSessionHandler wlSessionHandler;

    public HVBSessionHandler(final WLApiClient client, final HVBStorage storage, final WLConfig config) {
        wlSessionHandler = new WLSessionHandler(client, storage, config);
    }

    @Override
    public void logout() {
        wlSessionHandler.logout();
    }

    @Override
    public void keepAlive() throws SessionException {
        try {
            wlSessionHandler.heartbeat();
        } catch (HttpResponseException e) {
            throw SessionError.SESSION_EXPIRED.exception();
        }
    }
}
