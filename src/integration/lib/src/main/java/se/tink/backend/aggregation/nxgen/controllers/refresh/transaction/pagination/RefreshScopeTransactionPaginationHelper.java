package se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.libraries.credentials.service.RefreshScope;
import se.tink.libraries.credentials.service.TransactionsRefreshScope;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
@Slf4j
public class RefreshScopeTransactionPaginationHelper extends TransactionPaginationHelper {

    private final RefreshScope refreshScope;

    /**
     * Returns the lower limit (inclusive) date for this account. It can be specified by refresh
     * scope on all transaction or specifically on account transactions
     */
    @Override
    public Optional<Date> getTransactionDateLimit(Account account) {
        if (refreshScope == null || refreshScope.getTransactions() == null) {
            log.info("No transactions date limit, returning empty optional");
            return Optional.empty();
        }

        TransactionsRefreshScope transactionsRefreshScope = refreshScope.getTransactions();
        Optional<LocalDate> limit =
                transactionsRefreshScope.getTransactionBookedDateGteForAccountIdentifiers(
                        account.getIdentifiers());
        return limit.map(RefreshScopeTransactionPaginationHelper::localDateToDate);
    }

    private static Date localDateToDate(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay().toInstant(ZoneOffset.UTC));
    }
}
