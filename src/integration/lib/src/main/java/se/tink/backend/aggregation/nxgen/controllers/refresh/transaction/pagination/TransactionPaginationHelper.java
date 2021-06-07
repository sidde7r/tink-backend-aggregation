package se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination;

import com.google.common.annotations.VisibleForTesting;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.credentials.service.*;

@RequiredArgsConstructor
@Slf4j
public class TransactionPaginationHelper {
    @VisibleForTesting static final int SAFETY_THRESHOLD_NUMBER_OF_DAYS = 10;
    @VisibleForTesting static final int SAFETY_THRESHOLD_NUMBER_OF_OVERLAPS = 10;

    private final RefreshScope refreshScope;

    @Deprecated
    public TransactionPaginationHelper(HasRefreshScope hasRefreshScope) {
        this(hasRefreshScope.getRefreshScope());
    }

    public boolean shouldFetchNextPage(Account account, List<AggregationTransaction> transactions) {
        if (transactions.size() == 0) {
            return true;
        }

        final Optional<Date> transactionDateLimit = getTransactionDateLimit(account);

        if (!transactionDateLimit.isPresent()) {
            return true;
        }

        // Reached certain date and check next SAFETY_THRESHOLD_NUMBER_OF_OVERLAPS transactions
        // to not be after the previous one.

        AggregationTransaction lastTransaction = null;
        int transactionsBeforeCertainDate = 0;

        for (AggregationTransaction t : transactions) {

            if (lastTransaction == null) {

                if (t.getDate().before(transactionDateLimit.get())) {
                    lastTransaction = t;
                    transactionsBeforeCertainDate++;
                }
                continue;

            } else {

                // Certain date reached, check transaction is before last one.

                if (t.getDate().after(transactionDateLimit.get())) {

                    // If after, there is a gap in the paging. Start over again and
                    // find next transaction that is before certain date and do this again.

                    lastTransaction = null;
                    transactionsBeforeCertainDate = 0;

                } else {
                    transactionsBeforeCertainDate++;
                }
            }

            long overlappingTransactionDays =
                    Math.abs(
                            Duration.between(
                                            t.getDate().toInstant(),
                                            transactionDateLimit.get().toInstant())
                                    .toDays());

            if (transactionsBeforeCertainDate >= SAFETY_THRESHOLD_NUMBER_OF_OVERLAPS
                    && overlappingTransactionDays >= SAFETY_THRESHOLD_NUMBER_OF_DAYS) {
                return false;
            }
        }

        return true;
    }

    /**
     * Returns the lower limit (inclusive) date for this account. It can be specified by refresh
     * scope on all transaction or specifically on account transactions
     */
    public Optional<Date> getTransactionDateLimit(Account account) {
        if (refreshScope == null || refreshScope.getTransactions() == null) {
            return Optional.empty();
        }

        Optional<Date> defaultLimit =
                Optional.ofNullable(refreshScope.getTransactions().getTransactionBookedDateGte())
                        .map(TransactionPaginationHelper::localDateToDate);
        if (refreshScope.getTransactions().getAccounts() == null) {
            return defaultLimit;
        }

        Set<AccountIdentifier> accountIdentifiers = new HashSet<>(account.getIdentifiers());
        Optional<AccountTransactionsRefreshScope> accountRefreshScope =
                refreshScope.getTransactions().getAccounts().stream()
                        .filter(
                                it -> {
                                    Set<AccountIdentifier> refreshScopeAccountIdentifiers =
                                            it.getAccountIdentifiers().stream()
                                                    .map(AccountIdentifier::createOrThrow)
                                                    .collect(Collectors.toSet());
                                    return refreshScopeAccountIdentifiers.removeAll(
                                            accountIdentifiers);
                                })
                        .findAny();

        if (accountRefreshScope.isPresent()
                && accountRefreshScope.get().getTransactionBookedDateGte() != null) {
            return Optional.of(
                    localDateToDate(accountRefreshScope.get().getTransactionBookedDateGte()));
        }

        return defaultLimit;
    }

    private static Date localDateToDate(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay().toInstant(ZoneOffset.UTC));
    }
}
