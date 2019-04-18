package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.session;

import static se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaConstants.Messages;

import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities.ResultEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.rpc.BbvaResponse;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;

public class BbvaSessionHandler implements SessionHandler {
    private BbvaApiClient apiClient;

    public BbvaSessionHandler(BbvaApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public void logout() {
        apiClient.logout();
    }

    @Override
    public void keepAlive() throws SessionException {
        apiClient
                .initiateSession()
                .map(BbvaResponse::getResult)
                .map(ResultEntity::getCode)
                .filter(Messages.OK::equalsIgnoreCase)
                .getOrElseThrow(t -> SessionError.SESSION_EXPIRED.exception());
    }
}
