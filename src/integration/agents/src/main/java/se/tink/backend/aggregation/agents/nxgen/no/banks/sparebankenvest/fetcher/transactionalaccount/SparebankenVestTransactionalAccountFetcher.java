package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.fetcher.transactionalaccount;

import java.util.Collection;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.SparebankenVestApiClient;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.agents.rpc.Credentials;

public class SparebankenVestTransactionalAccountFetcher implements AccountFetcher<TransactionalAccount> {
    private static final AggregationLogger LOGGER =
            new AggregationLogger(SparebankenVestTransactionalAccountFetcher.class);

    private final SparebankenVestApiClient apiClient;
    private final Credentials credentials;

    private SparebankenVestTransactionalAccountFetcher(SparebankenVestApiClient apiClient, Credentials credentials) {
        this.apiClient = apiClient;
        this.credentials = credentials;
    }

    public static SparebankenVestTransactionalAccountFetcher create(SparebankenVestApiClient apiClient,
            Credentials credentials) {
        return new SparebankenVestTransactionalAccountFetcher(apiClient, credentials);
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        return this.apiClient.fetchAccounts().stream()
                .filter(AccountEntity::isDepositAccount)
                .map(AccountEntity::toTinkAccount)
                .collect(Collectors.toList());
    }
}
