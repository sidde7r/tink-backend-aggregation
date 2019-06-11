package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.session;

import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.CreditAgricoleApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.CreditAgricoleConstants;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth1Token;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public final class CreditAgricoleSessionHandler implements SessionHandler {
    private final CreditAgricoleApiClient apiClient;
    private final SessionStorage sessionStorage;

    public CreditAgricoleSessionHandler(
            CreditAgricoleApiClient apiClient, SessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public void logout() {}

    @Override
    public void keepAlive() throws SessionException {
        this.sessionStorage
                .get(CreditAgricoleConstants.StorageKeys.TEMPORARY_TOKEN, OAuth1Token.class)
                .orElseThrow(SessionError.SESSION_EXPIRED::exception);
    }
}
