package se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.fetcher.transaction;

import com.google.common.base.Strings;
import java.util.Date;
import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.CommerzbankApiClient;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.CommerzbankConstants.Headers;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.CommerzbankConstants.Tag;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.fetcher.transaction.entities.TransactionResultEntity;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

@AllArgsConstructor
public class CommerzbankTransactionFetcher
        implements TransactionDatePaginator<TransactionalAccount> {

    private static final AggregationLogger LOGGER =
            new AggregationLogger(CommerzbankTransactionFetcher.class);

    private final CommerzbankApiClient apiClient;

    @Override
    public PaginatorResponse getTransactionsFor(
            TransactionalAccount account, Date fromDate, Date toDate) {
        String productType = account.getFromTemporaryStorage(Headers.PRODUCT_TYPE);
        String identifier = account.getFromTemporaryStorage(Headers.IDENTIFIER);
        String productBranch = account.getFromTemporaryStorage(Headers.PRODUCT_BRANCH);

        if (!Strings.isNullOrEmpty(productType)
                && !Strings.isNullOrEmpty(identifier)
                && !Strings.isNullOrEmpty(productBranch)) {
            try {
                TransactionResultEntity response =
                        apiClient.fetchAllPages(
                                fromDate, toDate, productType, identifier, productBranch);
                if (response != null && response.containsTransactions()) {
                    return response.getItems().get(0);
                }

            } catch (Exception e) {
                LOGGER.errorExtraLong(
                        "Error fetching transactions", Tag.TRANSACTION_FETCHING_ERROR, e);
                return PaginatorResponseImpl.createEmpty();
            }
        }
        return PaginatorResponseImpl.createEmpty();
    }
}
