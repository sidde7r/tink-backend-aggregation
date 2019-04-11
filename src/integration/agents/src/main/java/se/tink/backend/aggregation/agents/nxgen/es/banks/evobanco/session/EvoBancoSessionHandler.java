package se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.session;

import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.EvoBancoApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.EvoBancoConstants;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.authenticator.entities.EeISessionMaintainerEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.authenticator.rpc.KeepAliveRequest;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class EvoBancoSessionHandler implements SessionHandler {

    private final EvoBancoApiClient apiClient;
    private final SessionStorage sessionStorage;

    public EvoBancoSessionHandler(EvoBancoApiClient apiClient, SessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public void logout() {}

    @Override
    public void keepAlive() throws SessionException {
        EeISessionMaintainerEntity eeISessionMaintainerEntity =
                new EeISessionMaintainerEntity(
                        sessionStorage.get(EvoBancoConstants.Storage.USER_BE),
                        sessionStorage.get(EvoBancoConstants.Storage.AGREEMENT_BE),
                        sessionStorage.get(EvoBancoConstants.Storage.ENTITY_CODE));

        KeepAliveRequest keepAliveRequest = new KeepAliveRequest(eeISessionMaintainerEntity);

        if (!apiClient.isAlive(keepAliveRequest)) {
            throw SessionError.SESSION_EXPIRED.exception();
        }
    }
}
