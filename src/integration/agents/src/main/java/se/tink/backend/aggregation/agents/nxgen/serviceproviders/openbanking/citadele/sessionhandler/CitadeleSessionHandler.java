package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.citadele.sessionhandler;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.citadele.CitadeleBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.citadele.CitadeleBaseConstants.ConsentStatus;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;

@AllArgsConstructor
public class CitadeleSessionHandler implements SessionHandler {

    private CitadeleBaseApiClient apiClient;
    private LocalDateTime expirationDate;

    @Override
    public void logout() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void keepAlive() throws SessionException {
        if (LocalDateTime.now().compareTo(expirationDate) >= 0
                || !apiClient.getConsentStatus().equals(ConsentStatus.VALID)) {
            throw SessionError.SESSION_EXPIRED.exception();
        }
    }
}
