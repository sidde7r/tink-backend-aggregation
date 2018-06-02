package se.tink.backend.system.workers.processor.storage;

import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
import java.util.concurrent.TimeUnit;
import se.tink.backend.common.repository.cassandra.TransactionCheckpointRepository;
import se.tink.libraries.uuid.UUIDUtils;
import se.tink.libraries.serialization.utils.SerializationUtils;
import se.tink.backend.connector.rpc.seb.PartnerTransactionPayload;
import se.tink.backend.core.CheckpointTransaction;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.User;
import se.tink.backend.system.workers.processor.TransactionProcessorCommand;
import se.tink.backend.system.workers.processor.TransactionProcessorCommandResult;
import se.tink.backend.system.workers.processor.TransactionProcessorContext;

public class SaveTransactionCheckpointCommand implements TransactionProcessorCommand {
    private final TransactionCheckpointRepository transactionCheckpointRepository;
    private final TransactionProcessorContext context;

    public SaveTransactionCheckpointCommand(
            TransactionProcessorContext context,
            TransactionCheckpointRepository transactionCheckpointRepository
    ) {
        this.context = context;
        this.transactionCheckpointRepository = transactionCheckpointRepository;
    }

    @Override
    public TransactionProcessorCommandResult execute(Transaction transaction) {
        String sebPayload = transaction.getInternalPayload(Transaction.InternalPayloadKeys.SEB_PAYLOAD);

        if (!Strings.isNullOrEmpty(sebPayload)) {
            PartnerTransactionPayload payload = SerializationUtils.deserializeFromString(sebPayload, PartnerTransactionPayload.class);
            if (payload.getCheckpointId() != null) {
                registerTransactionCheckpoint(payload.getCheckpointId(), transaction, context.getUser());
            }
        }

        return TransactionProcessorCommandResult.CONTINUE;
    }

    private void registerTransactionCheckpoint(String checkpointId, Transaction transactions, User user) {
        CheckpointTransaction mapping = new CheckpointTransaction();
        mapping.setCheckpointId(checkpointId);
        mapping.setTransactionId(UUIDUtils.fromTinkUUID(transactions.getId()));
        mapping.setUserId(UUIDUtils.fromTinkUUID(user.getId()));

        this.transactionCheckpointRepository.saveQuicklyWithTTL(mapping, 30, TimeUnit.DAYS);
    }

    @Override
    public void postProcess() {
        // Nothing.
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
