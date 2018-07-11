package se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.fetcher.transaction;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.CommerzbankApiClient;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.CommerzbankConstants;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.fetcher.transaction.entities.PfmTransactionsEntity;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.fetcher.transaction.entities.TransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.fetcher.transaction.entities.TransactionResultEntity;
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

        TransactionResultEntity transactionResultEntity = null;
        try {
            transactionResultEntity = apiClient.transactionOverview(productType, identifier);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        Preconditions.checkState(transactionResultEntity != null, Collections.EMPTY_LIST);
        Optional<TransactionEntity> transactionEntity = Optional.ofNullable(transactionResultEntity.getItems().get(0));
        Preconditions.checkState(transactionEntity != null, Collections.EMPTY_LIST);
        List<PfmTransactionsEntity> pfmTransactionsEntities = transactionEntity.get().getPfmTransactions();

        transactions = pfmTransactionsEntities.stream()
                .map(PfmTransactionsEntity::toTinkTransaction).collect(Collectors
                        .toList());
        return transactions;
    }
}
