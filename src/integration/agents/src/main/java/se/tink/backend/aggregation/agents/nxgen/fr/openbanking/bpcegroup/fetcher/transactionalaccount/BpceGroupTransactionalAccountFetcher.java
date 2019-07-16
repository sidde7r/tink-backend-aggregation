package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.fetcher.transactionalaccount;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.BpceGroupApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.fetcher.transactionalaccount.entity.accounts.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.fetcher.transactionalaccount.rpc.AccountsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class BpceGroupTransactionalAccountFetcher implements AccountFetcher<TransactionalAccount> {

    private final BpceGroupApiClient apiClient;

    public BpceGroupTransactionalAccountFetcher(BpceGroupApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {

        // Handling accounts pagination
        AccountsResponse accountsResponse;
        List<AccountEntity> accounts = new ArrayList<>();

        while ((accountsResponse = apiClient.fetchAccounts()).canFetchMore()) {
            accounts.addAll(accountsResponse.getAccounts());
        }

        return Stream.concat(accounts.stream(), accountsResponse.getAccounts().stream())
                .filter(AccountEntity::isTransactionalAccount)
                .filter(BpceGroupTransactionalAccountFetcher::sandboxFilter) // TODO remove for prod
                .map(AccountEntity::toTinkAccount)
                .collect(Collectors.toList());
    }

    // TODO remove for prod
    private static boolean sandboxFilter(AccountEntity accountEntity) {
        return !accountEntity.getIban().equalsIgnoreCase("FR7613807008043001965408165");
    }
}
