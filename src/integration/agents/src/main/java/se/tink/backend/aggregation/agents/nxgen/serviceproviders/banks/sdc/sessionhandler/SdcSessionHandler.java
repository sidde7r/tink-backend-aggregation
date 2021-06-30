package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.sessionhandler;

import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.SdcApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.rpc.FilterAccountsRequest;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

public class SdcSessionHandler implements SessionHandler {

    private final String market;
    private final SdcApiClient bankClient;

    public SdcSessionHandler(String market, SdcApiClient bankClient) {
        this.market = market;
        this.bankClient = bankClient;
    }

    @Override
    public void logout() {
        bankClient.logout();
    }

    @Override
    public void keepAlive() throws SessionException {
        try {
            if ("SE".equals(market)) {
                fetchAgreements();
            } else {
                fetchAccounts();
            }
        } catch (HttpResponseException e) {
            throw SessionError.SESSION_EXPIRED.exception(e);
        }
    }

    private void fetchAgreements() {
        bankClient.fetchAgreements();
    }

    private void fetchAccounts() {
        FilterAccountsRequest request =
                new FilterAccountsRequest()
                        .setIncludeCreditAccounts(false)
                        .setIncludeDebitAccounts(false)
                        .setOnlyFavorites(false)
                        .setOnlyQueryable(true);

        bankClient.filterAccounts(request);
    }
}
