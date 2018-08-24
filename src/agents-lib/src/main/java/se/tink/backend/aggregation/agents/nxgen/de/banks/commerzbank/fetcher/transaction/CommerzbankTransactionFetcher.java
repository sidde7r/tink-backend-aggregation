package se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.fetcher.transaction;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.base.Preconditions;
import java.util.Map;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.CommerzbankApiClient;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.CommerzbankConstants;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.fetcher.transaction.entities.TransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.fetcher.transaction.entities.TransactionResultEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginator;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;

public class CommerzbankTransactionFetcher implements TransactionPagePaginator<TransactionalAccount> {

    private final CommerzbankApiClient apiClient;

    public CommerzbankTransactionFetcher(CommerzbankApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public PaginatorResponse getTransactionsFor(TransactionalAccount account, int page) {
        Map<String, String> keys = account.getTemporaryStorage();
        String productType = keys.get(CommerzbankConstants.HEADERS.PRODUCT_TYPE).replaceAll("\"", "");
        String identifier = keys.get(CommerzbankConstants.HEADERS.IDENTIFIER).replaceAll("\"", "");

        TransactionResultEntity transactionResultEntity;
        try {
            transactionResultEntity = apiClient.transactionOverview(productType, identifier, page);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("The transaction search is invalid");
        }

        Preconditions.checkState(transactionResultEntity != null, "Transaction Result entity is null");
        Optional<TransactionEntity> transactionEntity = Optional.ofNullable(transactionResultEntity.getItems().get(0));

        return transactionEntity.orElse(new TransactionEntity());
    }

}
