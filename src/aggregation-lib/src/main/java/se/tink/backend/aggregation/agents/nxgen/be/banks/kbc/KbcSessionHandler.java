package se.tink.backend.aggregation.agents.nxgen.be.banks.kbc;

import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.filters.KbcHttpFilter;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;

public class KbcSessionHandler implements SessionHandler {
    private final KbcHttpFilter httpFilter;

    private KbcSessionHandler(KbcHttpFilter httpFilter) {
        this.httpFilter = httpFilter;
    }


    public static KbcSessionHandler create(KbcHttpFilter httpFilter) {
        return new KbcSessionHandler(httpFilter);
    }

    @Override
    public void logout() {
        // Make sure set the Token to null before we try to login again
        httpFilter.resetHttpFilter();
    }

    @Override
    public void keepAlive() throws SessionException {
        throw SessionError.SESSION_EXPIRED.exception();
    }
}
