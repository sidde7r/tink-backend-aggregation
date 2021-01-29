package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank;

import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc.ListAccountsRequest;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;

public class DanskeBankSessionHandler implements SessionHandler {
    private DanskeBankApiClient apiClient;
    private DanskeBankConfiguration configuration;

    public DanskeBankSessionHandler(
            DanskeBankApiClient apiClient, DanskeBankConfiguration configuration) {
        this.apiClient = apiClient;
        this.configuration = configuration;
    }

    @Override
    public void logout() {
        // nop
    }

    @Override
    public void keepAlive() throws SessionException {
        if (!apiClient.hasAuthorizationHeader()) {
            throw SessionError.SESSION_EXPIRED.exception();
        }

        try {
            apiClient.listAccounts(
                    ListAccountsRequest.createFromLanguageCode(configuration.getLanguageCode()));
        } catch (Exception e) {
            throw SessionError.SESSION_EXPIRED.exception(e);
        }
    }
}
