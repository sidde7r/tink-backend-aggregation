package se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination;

import com.google.common.annotations.VisibleForTesting;
import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;

public abstract class TransactionPaginationHelper {
    @VisibleForTesting static final int SAFETY_THRESHOLD_NUMBER_OF_DAYS = 10;
    @VisibleForTesting static final int SAFETY_THRESHOLD_NUMBER_OF_OVERLAPS = 10;

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

    public abstract Optional<Date> getTransactionDateLimit(Account account);
}
