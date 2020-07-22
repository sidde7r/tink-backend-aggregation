package se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.session;

import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.LansforsakringarApiClient;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

public class LansforsakringarSessionHandler implements SessionHandler {
    private final LansforsakringarApiClient apiCleint;

    public LansforsakringarSessionHandler(LansforsakringarApiClient apiCleint) {
        this.apiCleint = apiCleint;
    }

    @Override
    public void logout() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void keepAlive() throws SessionException {
        try {
            apiCleint.fetchAccounts();
        } catch (HttpResponseException ex) {
            if (ex.getResponse().getStatus() == HttpStatus.SC_UNAUTHORIZED) {
                throw SessionError.SESSION_EXPIRED.exception();
            }
        }
    }
}
