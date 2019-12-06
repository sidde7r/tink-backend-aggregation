package se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.fetcher.transactionalaccount;

import java.util.Collection;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.SkandiaApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.fetcher.transactionalaccount.rpc.GetAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.fetcher.transactionalaccount.rpc.GetBalancesResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class SkandiaTransactionalAccountFetcher implements AccountFetcher<TransactionalAccount> {

    private final SkandiaApiClient apiClient;

    public SkandiaTransactionalAccountFetcher(SkandiaApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        GetAccountsResponse accounts = apiClient.getAccounts();
        for (AccountEntity account : accounts.getAccounts()) {
            GetBalancesResponse balances = apiClient.getBalances(account.getResourceId());
            account.setBalances(balances.getBalances());
        }
        return accounts.toTinkAccounts();
    }
}
