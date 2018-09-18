package se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne;

import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.authenticator.rpc.ClientInformationRequest;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.authenticator.rpc.ClientInformationResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.authenticator.rpc.DisconnectRequest;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;

public class CaisseEpargneSessionHandler implements SessionHandler {

    private final CaisseEpargneApiClient apiClient;

    public CaisseEpargneSessionHandler(
            CaisseEpargneApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public void logout() {
        DisconnectRequest request = new DisconnectRequest();
        apiClient.disconnect(request);
    }

    @Override
    public void keepAlive() throws SessionException {
        ClientInformationRequest request = new ClientInformationRequest();
        ClientInformationResponse response = apiClient.getClientInformation(request);
        if (!response.isResponseOK()) {
            throw SessionError.SESSION_EXPIRED.exception();
        }
    }

}
