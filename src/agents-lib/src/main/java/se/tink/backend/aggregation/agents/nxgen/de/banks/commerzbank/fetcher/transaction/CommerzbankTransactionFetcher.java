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
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.index.TransactionIndexPaginator;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

public class CommerzbankTransactionFetcher implements TransactionIndexPaginator<TransactionalAccount> {

    private final CommerzbankApiClient apiClient;

    public CommerzbankTransactionFetcher(CommerzbankApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public PaginatorResponse getTransactionsFor(TransactionalAccount account, int numberOfTransactions,
            int startIndex) {
        Map<String, String> keys = account.getTemporaryStorage();
        String productType = keys.get(CommerzbankConstants.HEADERS.PRODUCT_TYPE).replaceAll("\"", "");
        String identifier = keys.get(CommerzbankConstants.HEADERS.IDENTIFIER).replaceAll("\"", "");

        TransactionResultEntity transactionResultEntity;
        try {
            transactionResultEntity = apiClient.transactionOverview(productType, identifier);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("The transaction search is invalid");
        }

        Preconditions.checkState(transactionResultEntity != null, "Transaction Result entity is null");
        Optional<TransactionEntity> transactionEntity = Optional.ofNullable(transactionResultEntity.getItems().get(0));

        List<PfmTransactionsEntity> pfmTransactionsEntities = transactionEntity
                .map(TransactionEntity::getPfmTransactions).orElse(new ArrayList<>());

        Collection<Transaction> transactions = pfmTransactionsEntities.stream()
                .map(PfmTransactionsEntity::toTinkTransaction).collect(Collectors
                        .toList());

        return PaginatorResponseImpl.create(transactions);
    }
}
