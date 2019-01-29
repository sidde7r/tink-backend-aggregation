package se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.fetcher.transactionalaccounts;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.BanquePopulaireApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.entities.ContractOverviewEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.fetcher.transactionalaccounts.rpc.BanquePopulaireTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.rpc.ContractsResponse;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class BanquePopulaireTransactionalAccountsFetcher implements AccountFetcher<TransactionalAccount>,
        TransactionKeyPaginator<TransactionalAccount, String> {
    private static final AggregationLogger LOGGER =
            new AggregationLogger(BanquePopulaireTransactionalAccountsFetcher.class);

    private final BanquePopulaireApiClient apiClient;

    public BanquePopulaireTransactionalAccountsFetcher(BanquePopulaireApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {

        ContractsResponse accountsResponse = apiClient.getAccountContracts();

        return accountsResponse.stream()
                .filter(account -> !account.isUnknownContractType())
                .map(ContractOverviewEntity::toTinkTransactionalAccount)
                .collect(Collectors.toList());
    }

    @Override
    public TransactionKeyPaginatorResponse<String> getTransactionsFor(TransactionalAccount account, String key) {
        return Optional.ofNullable(apiClient.getAccountTransactions(account, key))
                .orElse(BanquePopulaireTransactionsResponse.empty());
    }
}
