package se.tink.backend.aggregation.agents.nxgen.at.banks.bawagpsk.session;

import java.util.NoSuchElementException;
import javax.xml.bind.JAXBException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.at.banks.bawagpsk.BawagPskApiClient;
import se.tink.backend.aggregation.agents.nxgen.at.banks.bawagpsk.BawagPskConstants;
import se.tink.backend.aggregation.agents.nxgen.at.banks.bawagpsk.rpc.LogoutRequest;
import se.tink.backend.aggregation.agents.nxgen.at.banks.bawagpsk.rpc.ServiceResponse;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;

public class BawagPskSessionHandler implements SessionHandler {

    private BawagPskApiClient bawagPskApiClient;

    public BawagPskSessionHandler(BawagPskApiClient bawagPskApiClient) {
        this.bawagPskApiClient = bawagPskApiClient;
    }

    @Override
    public void logout() {
        final String serverSessionID;
        try {
            serverSessionID = bawagPskApiClient.getFromStorage(
                    BawagPskConstants.Storage.SERVER_SESSION_ID.name())
                    .orElseThrow(SessionError.SESSION_EXPIRED::exception);
        } catch (SessionException e) {
            return; // Session already expired; nothing to do
        }
        final LogoutRequest request = new LogoutRequest(serverSessionID);
        try {
            bawagPskApiClient.logout(request.getXml());
        } catch (JAXBException e) {
            throw new IllegalStateException("Unable to marshal JAXB ", e);
        }
    }

    private boolean hasSessionToken() {
        return bawagPskApiClient.getFromStorage(BawagPskConstants.Storage.SERVER_SESSION_ID.name()).isPresent();
    }

    @Override
    public void keepAlive() throws SessionException {
        if (!hasSessionToken()) {
            throw SessionError.SESSION_EXPIRED.exception(); // Assuming first login; no tokens yet
        }
        final ServiceResponse response;
        try {
            response = bawagPskApiClient.checkIfSessionAlive();
            if (!response.requestWasSuccessful()) {
                throw SessionError.SESSION_EXPIRED.exception();
            }
        } catch (NoSuchElementException e) {
            throw SessionError.SESSION_EXPIRED.exception();
        }
    }
}
