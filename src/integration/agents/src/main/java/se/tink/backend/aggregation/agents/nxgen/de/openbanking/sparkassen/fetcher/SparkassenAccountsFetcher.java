package se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.fetcher;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.SparkassenApiClient;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.SparkassenStorage;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.fetcher.detail.TransactionalAccountMapper;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.fetcher.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.fetcher.rpc.FetchBalancesResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class SparkassenAccountsFetcher implements AccountFetcher<TransactionalAccount> {

    private final SparkassenApiClient apiClient;
    private final SparkassenStorage storage;

    public SparkassenAccountsFetcher(SparkassenApiClient apiClient, SparkassenStorage storage) {
        this.apiClient = apiClient;
        this.storage = storage;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        return apiClient.fetchAccounts(storage.getConsentId()).getAccounts().stream()
                .map(this::enrichWithBalance)
                .map(
                        pair ->
                                TransactionalAccountMapper.toTinkAccountWithBalance(
                                        pair.getLeft(), pair.getRight()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private Pair<AccountEntity, FetchBalancesResponse> enrichWithBalance(
            AccountEntity accountEntity) {
        return new ImmutablePair<>(
                accountEntity,
                apiClient.getAccountBalance(storage.getConsentId(), accountEntity.getResourceId()));
    }
}
