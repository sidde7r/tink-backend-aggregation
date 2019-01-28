package se.tink.backend.aggregation.agents.nxgen.be.banks.kbc;

import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.filters.KbcHttpFilter;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;

public class KbcSessionHandler implements SessionHandler {
    private final KbcHttpFilter httpFilter;
    private final KbcApiClient apiClient;

    private KbcSessionHandler(KbcHttpFilter httpFilter, KbcApiClient apiClient) {
        this.httpFilter = httpFilter;
        this.apiClient = apiClient;
    }

    public static KbcSessionHandler create(KbcHttpFilter httpFilter, KbcApiClient apiClient) {
        return new KbcSessionHandler(httpFilter, apiClient);
    }

    @Override
    public void logout() {
        // Make sure set the Token to null before we try to login again
        httpFilter.resetHttpFilter();
    }

    @Override
    public void keepAlive() throws SessionException {
        try {
            apiClient.fetchAccounts(KbcConstants.DEFAULT_LANGUAGE_FOR_PARSE_ERROR_TEXTS);
        } catch (Exception e) {
            throw SessionError.SESSION_EXPIRED.exception();
        }
    }
}
