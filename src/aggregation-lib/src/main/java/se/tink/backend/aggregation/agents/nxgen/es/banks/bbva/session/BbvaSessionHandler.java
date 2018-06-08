package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.session;

import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaConstants;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.authenticator.BbvaAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.authenticator.rpc.InitiateSessionResponse;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;

import javax.xml.ws.http.HTTPException;
import java.util.Objects;

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
        try {
            InitiateSessionResponse response = apiClient.initiateSession();
            if (!Objects.equals(response.getResult().getCode(), BbvaConstants.Message.OK)) {
                throw SessionError.SESSION_EXPIRED.exception();
            }
        } catch (HttpClientException|HttpResponseException e) {
            throw SessionError.SESSION_EXPIRED.exception();
        }
    }
}
