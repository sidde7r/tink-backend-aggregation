package se.tink.backend.aggregation.agents.nxgen.se.banks.seb.session;

import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.se.banks.seb.SEBApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.seb.SEBConstants.ServiceInputValues;
import se.tink.backend.aggregation.agents.nxgen.se.banks.seb.SEBSessionStorage;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;

public class SEBSessionHandler implements SessionHandler {
    private SEBApiClient apiClient;
    private SEBSessionStorage sebSessionStorage;

    public SEBSessionHandler(SEBApiClient apiClient, SEBSessionStorage sebSessionStorage) {
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
            throw new SessionException(SessionError.SESSION_EXPIRED);
        }
    }
}
