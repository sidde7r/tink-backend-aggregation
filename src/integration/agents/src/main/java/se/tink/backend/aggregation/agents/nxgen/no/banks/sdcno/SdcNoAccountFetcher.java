package se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno;

import java.util.Collection;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.rpc.FilterAccountsRequest;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

class SdcNoAccountFetcher implements AccountFetcher<TransactionalAccount> {
    private final SdcNoApiClient bankClient;

    SdcNoAccountFetcher(SdcNoApiClient bankClient) {
        this.bankClient = bankClient;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        FilterAccountsRequest request =
                new FilterAccountsRequest()
                        .setIncludeCreditAccounts(true)
                        .setIncludeDebitAccounts(true)
                        .setOnlyFavorites(false)
                        .setOnlyQueryable(true);

        return bankClient.filterAccounts(request).getTinkAccounts();
    }
}
