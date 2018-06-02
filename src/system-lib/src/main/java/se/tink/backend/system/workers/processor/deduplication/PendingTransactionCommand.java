/**
 * 
 */
package se.tink.backend.system.workers.processor.deduplication;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Ordering;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import se.tink.backend.core.PendingStringTypes;
import se.tink.backend.core.Transaction;
import se.tink.backend.system.workers.processor.TransactionProcessorCommand;
import se.tink.backend.system.workers.processor.TransactionProcessorCommandResult;
import se.tink.backend.system.workers.processor.TransactionProcessorContext;
import se.tink.backend.system.workers.processor.utils.PendingTransactionHelper;
import se.tink.backend.utils.LogUtils;

/**
 * Goes through all transactions and removed old pending that now are
 * non-pending.
 * 
 * Sets
 *  isPending
 *  originalDescription
 *  originalAmount
 *  originalDate
 *  description
 *  amount
 *  category
 *  TransactionPayloadTypes.UNSETTLED_AMOUNT
 */
public class PendingTransactionCommand implements TransactionProcessorCommand {

    private static final LogUtils log = new LogUtils(PendingTransactionCommand.class);

    private static final Ordering<Transaction> orderByDateAsc = Ordering.natural().onResultOf(
            input -> Preconditions.checkNotNull(input.getDate()));

    private static final ImmutableSet<String> PRELIMINARY_STRINGS_LOWER_CASE = ImmutableSet.of(
            PendingStringTypes.HANDELSBANKEN.getValue().toLowerCase(),
            PendingStringTypes.SWEDBANK.getValue().toLowerCase(),
            PendingStringTypes.LANSFORSAKRINGAR.getValue().toLowerCase(),
            PendingStringTypes.SWEDBANK_PENDING_TRANSFER.getValue().toLowerCase());

    private final TransactionProcessorContext context;

    private HashMap<String, List<Transaction>> pendingTransactionsMap = new HashMap<String, List<Transaction>>();

    public PendingTransactionCommand(TransactionProcessorContext context) {
        this.context = context;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .toString();
    }

    @Override
    public TransactionProcessorCommandResult initialize() {

        // get all stored pending transactions for respective account

        Iterable<Transaction> inStoreTransactions = Iterables.filter(context.getUserData().getInStoreTransactions().values(),
                Transaction::isPending);

        for (Transaction t: inStoreTransactions) {
            if (pendingTransactionsMap.containsKey(t.getAccountId())) {
                pendingTransactionsMap.get(t.getAccountId()).add(t);
            } else {
                ArrayList<Transaction> list = new ArrayList<Transaction>();
                list.add(t);
                pendingTransactionsMap.put(t.getAccountId(), list);
            }

        }
        
        return TransactionProcessorCommandResult.CONTINUE;
    }

    @Override
    public TransactionProcessorCommandResult execute(Transaction transaction) {
        // do nothing here.
        return TransactionProcessorCommandResult.CONTINUE;
    }

    /**
     * Check all loaded transactions against the one we loaded in this batch.
     * Remove duplicated, that is transaction that was pending before but not
     * now.
     */
    @Override
    public void postProcess() {

        if (pendingTransactionsMap.size() == 0) {
            return;
        }

        // Only do this for pending transactions on accounts for this credential

        ListMultimap<String, Transaction> thisBatchTransactionsPerAccountMap = Multimaps.index(
                context.getInBatchTransactions(), Transaction::getAccountId);

        // Sorting this according to date here to avoid doing inside lots of loops in
        // findAndUpdateTheNonPendingTransaction(...).
        
        final ImmutableList<Transaction> dateSortedBatch = orderByDateAsc.reverse().immutableSortedCopy(
                context.getInBatchTransactions());

        for (String accountId : thisBatchTransactionsPerAccountMap.keySet()) {
            List<Transaction> pendingTransactionsByAccount = pendingTransactionsMap.get(accountId);
            if (pendingTransactionsByAccount == null || pendingTransactionsByAccount.isEmpty()) {
                continue;
            }

            Date oldestPendingDate = orderByDateAsc.min(pendingTransactionsByAccount).getOriginalDate();

            Iterable<Transaction> oldIterable = Iterables.filter(context.getUserData().getInStoreTransactions().values(),
                    PendingTransactionHelper.AfterOrSameDateAndSameAccountPredicate(oldestPendingDate, accountId));
            ArrayList<Transaction> oldTransactions = Lists.newArrayList(oldIterable);

            Iterable<Transaction> batchIterableSortedByDateDesc = Iterables.filter(dateSortedBatch,
                    PendingTransactionHelper.AfterOrSameDateAndSameAccountPredicate(oldestPendingDate, accountId));
            ArrayList<Transaction> thisBatchTransactionsSortedByDateDesc = Lists.newArrayList(batchIterableSortedByDateDesc);

            Transaction oldTrans;
            Iterator<Transaction> itOld;

            // Only handle the transaction with date newer than oldestLastPendingDate.

            for (Transaction thisBatchTrans : thisBatchTransactionsSortedByDateDesc) {
                itOld = oldTransactions.iterator();
                while (itOld.hasNext()) {
                    oldTrans = itOld.next();
                    if (PendingTransactionHelper.areSameTransaction(oldTrans, thisBatchTrans)) {
                        itOld.remove();
                        break;
                    }
                }
            }

            // The transactions that are left now from the old list were probably preliminary.
            // Check if we can find the non-preliminary transactions.

            if (!oldTransactions.isEmpty()) {
                itOld = oldTransactions.iterator();
                while (itOld.hasNext()) {
                    oldTrans = itOld.next();
                    if (PRELIMINARY_STRINGS_LOWER_CASE.contains(oldTrans.getOriginalDescription().toLowerCase())) {
                        itOld.remove();
                        PendingTransactionHelper.findAndUpdateTheNonPendingTransaction(context, oldTrans, thisBatchTransactionsSortedByDateDesc);
                    }
                }

                // If oldTransaction not 0, look in batch for match.

                if (!oldTransactions.isEmpty()) {
                    itOld = oldTransactions.iterator();
                    while (itOld.hasNext()) {
                        oldTrans = itOld.next();
                        if (oldTrans.isPending()
                                && PendingTransactionHelper.findOldPendingTransactionAndUpdate(context, oldTrans,
                                thisBatchTransactionsSortedByDateDesc)) {
                            itOld.remove();
                        }
                    }
                }

                // if still allTransactions is not empty, remove the pending
                // transactions since they should be duplicates
                for (Transaction t : oldTransactions) {
                    if (t.isPending()) {
                        context.addTransactionToDelete(t);
                        log.info(t.getUserId(), t.getCredentialsId(),
                                String.format("Removing transaction with Description: [%s] and Amount: [%s]",
                                        t.getDescription(), t.getAmount()));
                    }
                }
            }
        }
    }
}
