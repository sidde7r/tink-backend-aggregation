package se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.fetcher.transactionalaccount;

import java.util.Collection;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.SBABApiClient;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class SBABTransactionalAccountFetcher implements AccountFetcher<TransactionalAccount> {

    private final SBABApiClient apiClient;

    public SBABTransactionalAccountFetcher(SBABApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        return apiClient
                .fetchAccounts()
                .toTinkAccounts(apiClient.fetchCustomer().getCustomerName());
    }
}
