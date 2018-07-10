package se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.fetcher.transaction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.CommerzbankApiClient;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.CommerzbankConstants;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.fetcher.transaction.entities.PfmTransactionsEntity;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.fetcher.transaction.entities.TransactionEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.index.TransactionIndexPaginator;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

public class CommerzbankTransactionFetcher implements TransactionIndexPaginator<TransactionalAccount> {

    private final CommerzbankApiClient apiClient;

    public CommerzbankTransactionFetcher(CommerzbankApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<? extends Transaction> getTransactionsFor(TransactionalAccount account, int numberOfTransactions,
            int startIndex) {

        Collection<Transaction> transactions = new ArrayList<>();

        Map<String, String> keys = account.getTemporaryStorage();
        String productType = keys.get(CommerzbankConstants.HEADERS.PRODUCT_TYPE).replaceAll("\"", "");
        String identifier = keys.get(CommerzbankConstants.HEADERS.IDENTIFIER).replaceAll("\"", "");

        TransactionEntity transactionEntity = apiClient.transactionOverview(productType, identifier).getItems().get(0);
        List<PfmTransactionsEntity> pfmTransactionsEntities = transactionEntity.getPfmTransactions();

        transactions = pfmTransactionsEntities.stream()
                .map(pfmTransactionsEntity -> pfmTransactionsEntity.toTinkTransaction()).collect(Collectors
                        .toList());
        return transactions;
    }
}
