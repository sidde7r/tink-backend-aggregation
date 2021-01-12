package se.tink.backend.aggregation.agents.nxgen.se.banks.collector.fetcher.transactionalaccount;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.se.banks.collector.CollectorApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.collector.CollectorConstants;
import se.tink.backend.aggregation.agents.nxgen.se.banks.collector.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.collector.fetcher.transactionalaccount.rpc.SavingsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class CollectorTransactionalAccountsFetcher implements AccountFetcher<TransactionalAccount> {

    private final CollectorApiClient apiClient;
    private final SessionStorage sessionStorage;

    public CollectorTransactionalAccountsFetcher(
            CollectorApiClient apiClient, SessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        final String token = sessionStorage.get(CollectorConstants.Storage.BEARER_TOKEN);
        final List<AccountEntity> userAccounts = apiClient.fetchAccounts(token);

        return userAccounts.stream()
                .map(this::getAccountInfo)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private Optional<TransactionalAccount> getAccountInfo(AccountEntity accountEntity) {
        return Optional.of(accountEntity.getAccountUUID())
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(apiClient::getAccountInfo)
                .map(this::storeSavingsResponse)
                .flatMap(accountEntity::toTinkAccount);
    }

    private SavingsResponse storeSavingsResponse(SavingsResponse savingsResponse) {
        sessionStorage.put(
                CollectorConstants.Storage.TRANSACTIONS.concat(savingsResponse.getAccountNumber()),
                savingsResponse);
        return savingsResponse;
    }
}
