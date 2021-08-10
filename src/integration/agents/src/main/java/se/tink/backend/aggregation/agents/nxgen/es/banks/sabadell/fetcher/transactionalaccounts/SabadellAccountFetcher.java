package se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.transactionalaccounts;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.SabadellApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.transactionalaccounts.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.transactionalaccounts.rpc.AccountHoldersResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.transactionalaccounts.rpc.AccountsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class SabadellAccountFetcher implements AccountFetcher<TransactionalAccount> {
    private final SabadellApiClient apiClient;

    public SabadellAccountFetcher(SabadellApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        AccountsResponse accounts = apiClient.fetchAccounts();

        Stream<AccountEntity> accountEntities =
                accounts.getDivisas().stream()
                        .flatMap(divisaEntity -> divisaEntity.getAccounts().stream());
        return accountEntities
                .map(
                        accountEntity -> {
                            AccountHoldersResponse holders =
                                    apiClient.fetchAccountHolders(accountEntity);
                            return accountEntity.toTinkAccount(holders);
                        })
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }
}
