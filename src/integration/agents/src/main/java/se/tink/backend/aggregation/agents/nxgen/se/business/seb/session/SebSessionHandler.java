package se.tink.backend.aggregation.agents.nxgen.se.business.seb.session;

import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.se.business.seb.SebApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.business.seb.SebConstants.ServiceInputValues;
import se.tink.backend.aggregation.agents.nxgen.se.business.seb.SebSessionStorage;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;

public class SebSessionHandler implements SessionHandler {
    private final SebApiClient apiClient;
    private final SebSessionStorage sebSessionStorage;

    public SebSessionHandler(SebApiClient apiClient, SebSessionStorage sebSessionStorage) {
        this.apiClient = apiClient;
        this.sebSessionStorage = sebSessionStorage;
    }

    @Override
    public void logout() {}

    @Override
    public void keepAlive() throws SessionException {
        try {
            apiClient.fetchAccounts(
                    sebSessionStorage.getCustomerNumber(), ServiceInputValues.DEFAULT_ACCOUNT_TYPE);
        } catch (Exception e) {
            throw new SessionException(SessionError.SESSION_EXPIRED, e);
        }
    }
}
