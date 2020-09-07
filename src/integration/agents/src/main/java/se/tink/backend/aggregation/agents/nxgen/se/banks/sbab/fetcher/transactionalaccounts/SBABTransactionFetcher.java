package se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.fetcher.transactionalaccounts;

import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.SBABApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.fetcher.transactionalaccounts.entities.TransactionsEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.rpc.AccountsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;

public class SBABTransactionFetcher implements TransactionFetcher<TransactionalAccount> {
    private final SBABApiClient apiClient;

    public SBABTransactionFetcher(SBABApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public List<AggregationTransaction> fetchTransactionsFor(TransactionalAccount account) {
        final AccountsResponse accountsResponse = apiClient.fetchAccounts();

        return accountsResponse.getAccounts().getPersonalAccounts().stream()
                .filter(p -> p.getAccountNumber().equals(account.getAccountNumber()))
                .map(p -> p.getTransfers().getTransactions())
                .flatMap(List::stream)
                .map(TransactionsEntity::toTinkTransaction)
                .collect(Collectors.toList());
    }
}
