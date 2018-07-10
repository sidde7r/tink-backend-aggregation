package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v45.session;

import java.util.Optional;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v45.AmericanExpressApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v45.AmericanExpressConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v45.session.rpc.ExtendResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v45.session.rpc.SessionEntity;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class AmericanExpressSessionHandler implements SessionHandler {
    private final AmericanExpressApiClient apiClient;
    private final SessionStorage sessionStorage;

    public AmericanExpressSessionHandler(AmericanExpressApiClient apiClient,
            SessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public void logout() {
        apiClient.requestLogoff();
    }

    @Override
    public void keepAlive() throws SessionException {
        if ((sessionStorage.get(AmericanExpressConstants.Tags.SESSION_ID) != null)) {
            int status = Optional.of(apiClient.requestExtendSession())
                    .map(ExtendResponse::getExtendSession)
                    .map(SessionEntity::getStatus)
                    .orElseThrow(SessionError.SESSION_EXPIRED::exception);
            if (status == 0) {
                return;
            }
        }
        throw SessionError.SESSION_EXPIRED.exception();
    }

}
