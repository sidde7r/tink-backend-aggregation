package se.tink.backend.aggregation.agents.nxgen.be.openbanking.deutschebank.session;

import org.assertj.core.util.Strings;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.deutschebank.DeutscheBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.deutschebank.DeutscheBankConstants;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public final class DeutscheBankSessionHandler implements SessionHandler {

    private final DeutscheBankApiClient apiClient;
    private final SessionStorage sessionStorage;

    public DeutscheBankSessionHandler(
            DeutscheBankApiClient apiClient, SessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public void logout() {}

    @Override
    public void keepAlive() throws SessionException {
        if (Strings.isNullOrEmpty(
                this.sessionStorage.get(DeutscheBankConstants.StorageKeys.TOKEN))) {
            throw SessionError.SESSION_EXPIRED.exception();
        }
    }
}
