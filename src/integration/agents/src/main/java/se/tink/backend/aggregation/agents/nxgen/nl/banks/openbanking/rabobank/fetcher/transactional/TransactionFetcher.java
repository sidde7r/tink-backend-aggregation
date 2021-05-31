package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.fetcher.transactional;

import java.util.Date;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.RabobankApiClient;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.RabobankConstants;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

@Slf4j
public final class TransactionFetcher implements TransactionDatePaginator<TransactionalAccount> {

    private final RabobankApiClient apiClient;

    public TransactionFetcher(final RabobankApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public PaginatorResponse getTransactionsFor(
            final TransactionalAccount account, final Date fromDate, final Date toDate) {

        if (!hasTransactionConsent(account)) {
            log.info(
                    "Transactions consent not granted for account, no transactions will be fetched");
            return PaginatorResponseImpl.createEmpty(false);
        }

        return apiClient.getTransactions(account, fromDate, toDate, false);
    }

    private boolean hasTransactionConsent(TransactionalAccount account) {
        Optional<Boolean> hasTransactionsConsent =
                account.getFromTemporaryStorage(
                        RabobankConstants.StorageKey.HAS_TRANSACTIONS_CONSENT, Boolean.class);

        return hasTransactionsConsent.isPresent()
                && Boolean.TRUE.equals(hasTransactionsConsent.get());
    }
}
