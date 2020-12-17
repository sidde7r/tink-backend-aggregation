package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.fetcher.transactionalaccount;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.SparebankenVestApiClient;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class SparebankenVestTransactionalAccountFetcher
        implements AccountFetcher<TransactionalAccount> {
    private final SparebankenVestApiClient apiClient;

    private SparebankenVestTransactionalAccountFetcher(SparebankenVestApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public static SparebankenVestTransactionalAccountFetcher create(
            SparebankenVestApiClient apiClient) {
        return new SparebankenVestTransactionalAccountFetcher(apiClient);
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        return this.apiClient.fetchAccounts().getAccountEntities().stream()
                .map(AccountEntity::toTinkAccount)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }
}
