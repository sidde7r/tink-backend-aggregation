package se.tink.backend.system.workers.processor.system;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Iterables;
import com.google.common.collect.Ordering;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import se.tink.backend.core.Transaction;
import se.tink.libraries.date.DateUtils;
import se.tink.backend.utils.guavaimpl.Predicates;

public class CertainDateCalculator {

    // TODO: Simplify.
    // TODO: Convert to Java ordering.
    private static final Ordering<Transaction> TRANSACTION_ORDERING = new Ordering<Transaction>() {
        @Override
        public int compare(Transaction left, Transaction right) {
            return ComparisonChain.start().compare(left.getOriginalDate(), right.getOriginalDate())
                    .compare(left.getDescription(), right.getDescription()).compare(left.getId(), right.getId())
                    .result();
        }
    };

    static Date calculateCertainDate(List<Transaction> transactions) {
        // TODO: If this method is slow, avoid ordering `transactions` here, but instead make the streams below
        // sorted below. Will go from O(n*log(n)) to O(n).
        List<Transaction> sortedTransactions = TRANSACTION_ORDERING.reverse().sortedCopy(transactions);

        Optional<Transaction> latestNonPendingTransaction = sortedTransactions.stream()
                .filter(Predicates.filterTransactionsOnIsPending(false)::apply).findFirst();

        if (!latestNonPendingTransaction.isPresent()) {
            return null;
        }

        Date certainDate = DateUtils.addDays(latestNonPendingTransaction.get().getOriginalDate(), -30);

        // Get the latest date of thirty days before the last transaction, and the date fifty transactions ago.

        Transaction fiftyTransactionsAgo = Iterables.get(sortedTransactions, 49, null);

        if (fiftyTransactionsAgo != null) {
            certainDate = DateUtils.max(certainDate, fiftyTransactionsAgo.getOriginalDate());
        }

        // Find the first pending transactions, if any.

        Optional<Transaction> firstPendingTransaction = sortedTransactions.stream()
                .filter(Predicates.filterTransactionsOnIsPending(true)::apply)
                .reduce((a, b) -> b);

        if (firstPendingTransaction.isPresent()) {
            Date firstPendingDate = firstPendingTransaction.get().getOriginalDate();
            if (firstPendingDate.before(certainDate) || DateUtils.isSameDay(certainDate, firstPendingDate)) {
                certainDate = DateUtils.addDays(firstPendingDate, -1);
            }
        }

        // In case there are future transactions that are non-pending, make sure certain date < today.

        Date today = new Date();

        if (certainDate.after(today)) {
            certainDate = DateUtils.addDays(today, -30);
        }

        return certainDate;
    }
}
