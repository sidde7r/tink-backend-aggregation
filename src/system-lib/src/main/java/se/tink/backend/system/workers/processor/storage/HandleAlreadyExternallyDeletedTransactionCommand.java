package se.tink.backend.system.workers.processor.storage;

import com.google.common.base.MoreObjects;
import se.tink.backend.common.repository.cassandra.ExternallyDeletedTransactionRepository;
import se.tink.backend.common.repository.cassandra.TransactionExternalIdRepository;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.TransactionExternalId;
import se.tink.backend.core.TransactionPayloadTypes;
import se.tink.backend.system.workers.processor.TransactionProcessorCommand;
import se.tink.backend.system.workers.processor.TransactionProcessorCommandResult;
import se.tink.backend.utils.LogUtils;

/**
 * Handles the case where the transaction to be processed has already been deleted from an external source. This should
 * be an unusual case, but could happen because of queued transactions not being in order or invalid requests.
 */
public class HandleAlreadyExternallyDeletedTransactionCommand implements TransactionProcessorCommand {

    private ExternallyDeletedTransactionRepository externallyDeletedTransactionRepository;
    private TransactionExternalIdRepository transactionExternalIdRepository;

    private static final LogUtils log = new LogUtils(HandleAlreadyExternallyDeletedTransactionCommand.class);

    public HandleAlreadyExternallyDeletedTransactionCommand(
            ExternallyDeletedTransactionRepository externallyDeletedTransactionRepository,
            TransactionExternalIdRepository transactionExternalIdRepository
    ) {
        this.externallyDeletedTransactionRepository = externallyDeletedTransactionRepository;
        this.transactionExternalIdRepository = transactionExternalIdRepository;
    }

    @Override
    public TransactionProcessorCommandResult execute(Transaction transaction) {
        String externalTransactionId = transaction.getPayload().get(TransactionPayloadTypes.EXTERNAL_ID);

        if (externalTransactionId != null) {
            TransactionExternalId transactionExternalId = transactionExternalIdRepository
                    .findByAccountIdUserIdAndExternalTransactionId(transaction.getAccountId(), transaction.getUserId(),
                            externalTransactionId);

            // TODO Remove externallyDeletedTransactionRepository when no necessary
            if (externallyDeletedTransactionRepository.findByAccountIdUserIdAndExternalTransactionId(
                    transaction.getAccountId(), transaction.getUserId(), externalTransactionId) != null
                    || transactionExternalId != null && transactionExternalId.isDeleted()) {

                log.info(transaction.getUserId(), String.format(
                        "Skipping transaction since it has already been marked as deleted (accountId=%s, externalId=%s).",
                        transaction.getAccountId(), externalTransactionId));

                return TransactionProcessorCommandResult.BREAK;
            }
        }

        return TransactionProcessorCommandResult.CONTINUE;
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
