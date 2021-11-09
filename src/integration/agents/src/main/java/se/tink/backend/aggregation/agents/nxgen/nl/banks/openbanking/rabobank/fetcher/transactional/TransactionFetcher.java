package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.fetcher.transactional;

import java.util.Date;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.RabobankApiClient;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.RabobankConstants;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

@Slf4j
@RequiredArgsConstructor
public final class TransactionFetcher implements TransactionDatePaginator<TransactionalAccount> {

    private final RabobankApiClient apiClient;
    private final Date dateLimit;
    private final boolean isUserPresent;
    private boolean hasFetchedOnce = false;

    @Override
    public PaginatorResponse getTransactionsFor(
            final TransactionalAccount account, final Date fromDate, final Date toDate) {

        if (whenTransactionConsentNotGranted(account) || fromDate.before(dateLimit)) {
            return PaginatorResponseImpl.createEmpty(false);
        }
        if (hasFetchedOnce && !isUserPresent) {
            return PaginatorResponseImpl.createEmpty(false);
        }

        PaginatorResponse transactionsResponse =
                apiClient.getTransactions(account, calculateFromDate(fromDate), toDate, false);
        hasFetchedOnce = true;
        return transactionsResponse;
    }

    private Date calculateFromDate(Date fromDate) {
        if (fromDate.before(dateLimit)) {
            return dateLimit;
        }
        return fromDate;
    }

    private boolean whenTransactionConsentNotGranted(TransactionalAccount account) {
        Optional<Boolean> hasTransactionsConsent =
                account.getFromTemporaryStorage(
                        RabobankConstants.StorageKey.HAS_TRANSACTIONS_CONSENT, Boolean.class);

        final boolean isTransactionConsentNotGranted =
                hasTransactionsConsent.isPresent()
                        && Boolean.FALSE.equals(hasTransactionsConsent.get());
        if (isTransactionConsentNotGranted) {
            log.info(
                    "Transactions consent not granted for account, no transactions will be fetched");
        }
        return isTransactionConsentNotGranted;
    }
}
