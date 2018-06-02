package se.tink.backend.system.workers.processor.deduplication;

import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import java.util.Set;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.TransactionPayloadTypes;
import se.tink.backend.system.workers.processor.TransactionProcessorCommand;
import se.tink.backend.system.workers.processor.TransactionProcessorCommandResult;
import se.tink.backend.utils.LogUtils;

/**
 * Command that will remove duplicates from the incoming transactions from ABN AMRO. Transactions are considered
 * duplicates if they belong to the same account and have the same `EXTERNAL_ID`. If two transactions are duplicates
 * then processing will continue on one of them.
 * <p>
 * The `DeduplicationCommand` is only comparing the incoming transactions to the transactions that we already have in
 * the database (InStore transactions), this command only compares the new transactions (InBatch).
 */
public class AbnAmroDuplicateTransactionCommand implements TransactionProcessorCommand {

    private static final LogUtils log = new LogUtils(AbnAmroDuplicateTransactionCommand.class);

    private Set<String> transactions = Sets.newHashSet();

    public TransactionProcessorCommandResult execute(Transaction transaction) {

        final String externalId = transaction.getPayloadValue(TransactionPayloadTypes.EXTERNAL_ID);
        final String accountId = transaction.getAccountId();

        if (Strings.isNullOrEmpty(externalId) || Strings.isNullOrEmpty(accountId)) {
            return TransactionProcessorCommandResult.CONTINUE;
        }

        String key = createKey(externalId, accountId);

        if (transactions.contains(key)) {
            log.warn(transaction.getUserId(), String.format("Duplicate transaction found. %s", transaction));
            return TransactionProcessorCommandResult.BREAK;
        }

        transactions.add(key);
        return TransactionProcessorCommandResult.CONTINUE;
    }

    private static String createKey(String externalId, String accountId) {
        return String.format("%s.%s", externalId, accountId);
    }

    @Override
    public TransactionProcessorCommandResult initialize() {
        return TransactionProcessorCommandResult.CONTINUE;
    }

    /**
     * Called for every command in command chain's reverse order at after processing all transactions.
     */
    @Override
    public void postProcess() {
        // Deliberately left empty.
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).toString();
    }
}
