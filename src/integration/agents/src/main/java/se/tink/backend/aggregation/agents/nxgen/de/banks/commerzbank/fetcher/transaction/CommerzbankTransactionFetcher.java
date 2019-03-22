package se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.fetcher.transaction;

import com.google.common.base.Strings;
import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.CommerzbankApiClient;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.CommerzbankConstants;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.fetcher.transaction.entities.TransactionResultEntity;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class CommerzbankTransactionFetcher
        implements TransactionDatePaginator<TransactionalAccount> {

    private final CommerzbankApiClient apiClient;
    private static final AggregationLogger LOGGER =
            new AggregationLogger(CommerzbankTransactionFetcher.class);

    public CommerzbankTransactionFetcher(CommerzbankApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public PaginatorResponse getTransactionsFor(
            TransactionalAccount account, Date fromDate, Date toDate) {
        String productType =
                account.getFromTemporaryStorage(CommerzbankConstants.HEADERS.PRODUCT_TYPE);
        String identifier =
                account.getFromTemporaryStorage(CommerzbankConstants.HEADERS.IDENTIFIER);
        String productBranch =
                account.getFromTemporaryStorage(CommerzbankConstants.HEADERS.PRODUCT_BRANCH);

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
                        "Error fetching transactions",
                        CommerzbankConstants.LOGTAG.TRANSACTION_FETCHING_ERROR,
                        e);
                return PaginatorResponseImpl.createEmpty();
            }
        }
        return PaginatorResponseImpl.createEmpty();
    }
}
