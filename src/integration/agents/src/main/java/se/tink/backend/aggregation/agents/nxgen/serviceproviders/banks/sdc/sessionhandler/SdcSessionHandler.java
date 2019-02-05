package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.sessionhandler;

import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.SdcApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.rpc.FilterAccountsRequest;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;

public class SdcSessionHandler implements SessionHandler {

    private final SdcApiClient bankClient;

    public SdcSessionHandler(SdcApiClient bankClient) {

        this.bankClient = bankClient;
    }

    @Override
    public void logout() {
        bankClient.logout();
    }

    @Override
    public void keepAlive() throws SessionException {
        // use filter request to check if the session is still alive
        // any error is deemed session expired
        try {
            FilterAccountsRequest request = new FilterAccountsRequest()
                    .setIncludeDebitAccounts(true)
                    .setOnlyFavorites(true)
                    .setOnlyQueryable(true);

            bankClient.filterAccounts(request);
        } catch (Exception e) {
            throw SessionError.SESSION_EXPIRED.exception();
        }
    }
}
