package se.tink.backend.system.workers.processor.storage;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import java.util.Collection;
import se.tink.backend.common.dao.transactions.TransactionDao;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.User;
import se.tink.backend.system.workers.processor.TransactionProcessorCommand;
import se.tink.backend.system.workers.processor.TransactionProcessorCommandResult;
import se.tink.backend.system.workers.processor.TransactionProcessorContext;
import se.tink.backend.utils.LogUtils;
import se.tink.libraries.metrics.Counter;
import se.tink.libraries.metrics.MetricId;
import se.tink.libraries.metrics.MetricRegistry;

public class SaveTransactionCommand implements TransactionProcessorCommand {
    private static final int A_LOT_OF_TRANSACTIONS = 1000;
    private static final LogUtils log = new LogUtils(SaveTransactionCommand.class);

    private final TransactionDao transactionDao;
    private final TransactionProcessorContext context;
    private final Counter transactionsSaved;
    private final Counter transactionsDeleted;

    private User user;

    public SaveTransactionCommand(TransactionProcessorContext context,
            TransactionDao transactionDao, MetricRegistry registry) {
        this.context = context;
        this.transactionDao = transactionDao;

        this.transactionsSaved = registry.meter(MetricId.newId("transactions_saved"));
        this.transactionsDeleted = registry.meter(MetricId.newId("transactions_deleted"));
    }

    @Override
    public TransactionProcessorCommandResult execute(Transaction transaction) {
        context.getTransactionsToSave().put(transaction.getId(), transaction);
        return TransactionProcessorCommandResult.CONTINUE;
    }

    @Override
    public void postProcess() {
        user = context.getUser();

        save(context.getTransactionsToSave().values());
        delete(context.getTransactionsToDelete());
    }

    private void save(Collection<Transaction> transactionsToSave) {
        // Assert all transactions to save are healthy. Unfortunately Cassandra doesn't manage nullability on fields...
        for (Transaction transaction : transactionsToSave) {
            Preconditions.checkNotNull(transaction.getType());
            Preconditions.checkNotNull(transaction.getDescription());
            Preconditions.checkNotNull(transaction.getDate());
        }

        log.info(user.getId(), String.format("Saving %s transactions.", transactionsToSave.size()));

        transactionsSaved.inc(transactionsToSave.size());
        transactionDao.saveAndIndex(user, transactionsToSave, true);
    }
    
    private void delete(Collection<Transaction> transactionsToDelete) {
        if (transactionsToDelete.isEmpty()) {
            log.info(user.getId(), "No transactions to delete");
            return;
        }
        if (transactionsToDelete.size() > A_LOT_OF_TRANSACTIONS) {
            log.warn(user.getId(), "Removing surprisingly many transactions: " + transactionsToDelete.size());
        }

        transactionsDeleted.inc(transactionsToDelete.size());
        transactionDao.delete(transactionsToDelete);
    }

    @Override
    public TransactionProcessorCommandResult initialize() {
        return TransactionProcessorCommandResult.CONTINUE;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).toString();
    }
}
