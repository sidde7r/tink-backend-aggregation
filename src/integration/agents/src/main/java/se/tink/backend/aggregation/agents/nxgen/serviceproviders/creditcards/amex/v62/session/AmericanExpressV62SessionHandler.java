package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.session;

import java.util.Optional;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.AmericanExpressV62ApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.AmericanExpressV62Constants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.session.rpc.ExtendResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.session.rpc.SessionEntity;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class AmericanExpressV62SessionHandler implements SessionHandler {
    private final AmericanExpressV62ApiClient apiClient;
    private final SessionStorage sessionStorage;

    public AmericanExpressV62SessionHandler(
            AmericanExpressV62ApiClient apiClient, SessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public void logout() {
        apiClient.requestLogoff();
    }

    @Override
    public void keepAlive() throws SessionException {

        Optional.ofNullable(sessionStorage.get(AmericanExpressV62Constants.Tags.SESSION_ID))
                .orElseThrow(SessionError.SESSION_EXPIRED::exception);

        int status =
                Optional.of(apiClient.requestExtendSession())
                        .map(ExtendResponse::getExtendSession)
                        .map(SessionEntity::getStatus)
                        .orElseThrow(SessionError.SESSION_EXPIRED::exception);
        if (status != 0) {
            throw SessionError.SESSION_EXPIRED.exception();
        }
    }
}
