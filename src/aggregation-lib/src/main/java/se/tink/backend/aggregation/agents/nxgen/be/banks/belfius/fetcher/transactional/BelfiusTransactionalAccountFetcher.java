package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.fetcher.transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.BelfiusApiClient;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.fetcher.transactional.entities.BelfiusTransaction;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.fetcher.transactional.rpc.FetchTransactionsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcher;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;

public class BelfiusTransactionalAccountFetcher implements AccountFetcher<TransactionalAccount>,
        TransactionFetcher<TransactionalAccount> {

    private final BelfiusApiClient apiClient;

    public BelfiusTransactionalAccountFetcher(BelfiusApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        return this.apiClient.fetchProducts().stream()
                .filter(entry -> entry.getValue().isTransactionalAccount())
                .map(entry -> entry.getValue().toTransactionalAccount(entry.getKey()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public List<AggregationTransaction> fetchTransactionsFor(TransactionalAccount account) {
        List<AggregationTransaction> transactionsAll = new ArrayList<>();
        String key = account.getBankIdentifier();
        boolean initialRequest = true;
        FetchTransactionsResponse response;

        do {
            response = apiClient.fetchTransactions(key, initialRequest);

            List<AggregationTransaction> transactionsPage = response.stream()
                    .map(BelfiusTransaction::toTinkTransaction)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            transactionsAll.addAll(transactionsPage);
            initialRequest = false;

        } while (response.hasNext());

        return transactionsAll;
    }
}
