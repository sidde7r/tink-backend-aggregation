package se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.fetcher.transactionalaccount;

import java.util.Collection;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.SbabApiClient;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class SbabTransactionalAccountFetcher implements AccountFetcher<TransactionalAccount> {

    private final SbabApiClient apiClient;

    public SbabTransactionalAccountFetcher(SbabApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        return apiClient
                .fetchAccounts()
                .toTinkAccounts(apiClient.fetchCustomer().getCustomerName());
    }
}
