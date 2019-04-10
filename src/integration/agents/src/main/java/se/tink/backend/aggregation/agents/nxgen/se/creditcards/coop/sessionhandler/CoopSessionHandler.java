package se.tink.backend.aggregation.agents.nxgen.se.creditcards.coop.sessionhandler;

import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.se.creditcards.coop.CoopApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.creditcards.coop.CoopConstants;
import se.tink.backend.aggregation.agents.nxgen.se.creditcards.coop.rpc.UserSummaryResponse;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class CoopSessionHandler implements SessionHandler {

    private final CoopApiClient apiClient;
    private final SessionStorage sessionStorage;

    public CoopSessionHandler(CoopApiClient apiClient, SessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public void logout() {}

    @Override
    public void keepAlive() throws SessionException {
        try {
            UserSummaryResponse userSummary = apiClient.getUserSummary();
            sessionStorage.put(CoopConstants.Storage.USER_SUMMARY, userSummary);

        } catch (Exception e) {
            throw SessionError.SESSION_EXPIRED.exception();
        }
    }
}
