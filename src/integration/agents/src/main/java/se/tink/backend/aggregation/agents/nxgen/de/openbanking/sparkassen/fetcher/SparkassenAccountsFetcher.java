package se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.fetcher;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.SparkassenApiClient;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.fetcher.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.fetcher.rpc.FetchBalancesResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class SparkassenAccountsFetcher implements AccountFetcher<TransactionalAccount> {

    private final SparkassenApiClient apiClient;

    public SparkassenAccountsFetcher(SparkassenApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        return apiClient.fetchAccounts().getAccounts().stream()
                .map(this::toTransactionalAccount)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private Optional<TransactionalAccount> toTransactionalAccount(AccountEntity accountEntity) {
        FetchBalancesResponse fetchBalancesResponse =
                apiClient.getAccountBalance(accountEntity.getResourceId());
        return accountEntity.toTinkAcount(fetchBalancesResponse);
    }
}
