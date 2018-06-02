package se.tink.backend.system.workers.processor.utils;

import com.google.common.base.Predicate;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang.time.DateUtils;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.TransactionPayloadTypes;
import se.tink.backend.system.workers.processor.TransactionProcessorContext;
import se.tink.backend.utils.StringUtils;

public class PendingTransactionHelper {

    private static int DATE_FACTOR = 3;
    private static double JARO_WINKLER_FACTOR = 0.7;
    private static final double AMOUNT_EQUALS_DELTA = 0.001;

    /**
     * The description has changed completely, look for date and amount, start with the latest transaction first.
     */
    public static void findAndUpdateTheNonPendingTransaction(TransactionProcessorContext context,
            Transaction oldPending,
            List<Transaction> thisBatchTransactionsSortedByDateDesc) {

        for (Transaction t : thisBatchTransactionsSortedByDateDesc) {
            if (amountsAreClose(oldPending.getOriginalAmount(), t.getOriginalAmount())
                    && datesAreClose(oldPending.getOriginalDate(), t.getOriginalDate())
                    && oldPending.isPending() != t.isPending() && oldPending.getAccountId().equals(t.getAccountId())) {

                updateOldPendingTransaction(context, t, oldPending);

                // Remove transaction so that we don't match with it again if we have several potential matches in the
                // same batch.

                thisBatchTransactionsSortedByDateDesc.remove(t);

                break;
            }
        }
    }

    /**
     * A pending transaction can have changed name, amount and date.
     */
    public static boolean findOldPendingTransactionAndUpdate(TransactionProcessorContext context, Transaction pendingTransaction,
            List<Transaction> thisBatchTransactions) {

        for (Transaction thisBatchTransaction : thisBatchTransactions) {
            if (isOldPendingTransaction(pendingTransaction, thisBatchTransaction)) {
                updateOldPendingTransaction(context, thisBatchTransaction, pendingTransaction);
                return true;
            }
        }
        return false;
    }

    public static boolean isOldPendingTransaction(Transaction pendingTransaction, Transaction thisBatchTransaction) {
         return StringUtils.getJaroWinklerDistance(pendingTransaction.getOriginalDescription(),
                thisBatchTransaction.getOriginalDescription()) > JARO_WINKLER_FACTOR
                && datesAreClose(pendingTransaction.getOriginalDate(), thisBatchTransaction.getOriginalDate())
                && amountsAreClose(pendingTransaction.getOriginalAmount(), thisBatchTransaction.getOriginalAmount())
                && pendingTransaction.isPending() != thisBatchTransaction.isPending()
                && pendingTransaction.getAccountId().equals(thisBatchTransaction.getAccountId());
    }

    public static void updateOldPendingTransaction(TransactionProcessorContext context, Transaction transaction,
            Transaction pendingTransaction) {

        pendingTransaction.setOriginalDescription(transaction.getOriginalDescription());
        pendingTransaction.setPending(false);
        pendingTransaction.setPayload(TransactionPayloadTypes.UNSETTLED_AMOUNT,
                String.valueOf(pendingTransaction.getOriginalAmount()));
        pendingTransaction.setOriginalAmount(transaction.getOriginalAmount());
        pendingTransaction.setOriginalDate(transaction.getOriginalDate());

        if (!pendingTransaction.isUserModifiedDescription()) {
            pendingTransaction.setDescription(transaction.getDescription());
        }

        if (!pendingTransaction.isUserModifiedAmount()) {
            pendingTransaction.setAmount(transaction.getAmount());
        }

        if (!pendingTransaction.isUserModifiedCategory() && transaction.getCategoryId() != null) {
            pendingTransaction.setCategoryId(transaction.getCategoryId());
        }

        context.addTransactionToDelete(transaction);
        context.addTransactionToUpdateListPresentInDb(pendingTransaction.getId());
    }

    private static boolean datesAreClose(Date pendingTransDate, Date transDate) {
        for (int i = 0; i <= DATE_FACTOR; i++) {
            Date newDayToCheck = DateUtils.addDays(transDate, -i);
            if (DateUtils.isSameDay(newDayToCheck, pendingTransDate)) {
                return true;
            }
        }
        return false;
    }

    private static boolean amountsAreClose(double pendingTransAmount, double transAmount) {
        Double transAmountAbs = Math.abs(transAmount);
        Double pendingTransAmountAbs = Math.abs(pendingTransAmount);

        // Deliberately truncating the floating point here (and not rounding).

        if (transAmountAbs.intValue() == pendingTransAmountAbs.intValue()) {
            return true;
        }
        return false;
    }

    public static boolean areSameTransaction(Transaction ta, Transaction tb) {
        return ta.getOriginalDescription().equals(tb.getOriginalDescription())
                && DateUtils.isSameDay(ta.getOriginalDate(), tb.getOriginalDate())
                && ta.getAccountId().equals(tb.getAccountId())
                && Math.abs(ta.getOriginalAmount() - tb.getOriginalAmount()) < AMOUNT_EQUALS_DELTA
                && ta.isPending() == tb.isPending();
    }

    public static final Predicate<Transaction>  AfterOrSameDateAndSameAccountPredicate(
            final Date compareDate, final String accountId) {
        return transaction ->
                !transaction.getOriginalDate().before(compareDate) && transaction.getAccountId().equals(accountId);
    }
}
