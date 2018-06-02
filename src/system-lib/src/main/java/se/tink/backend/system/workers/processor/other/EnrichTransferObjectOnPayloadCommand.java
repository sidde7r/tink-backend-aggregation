package se.tink.backend.system.workers.processor.other;

import com.google.common.base.MoreObjects;
import java.util.Map;
import se.tink.libraries.uuid.UUIDUtils;
import se.tink.libraries.serialization.utils.SerializationUtils;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.TransactionPayloadTypes;
import se.tink.backend.core.transfer.Transfer;
import se.tink.backend.system.workers.processor.TransactionProcessorCommand;
import se.tink.backend.system.workers.processor.TransactionProcessorCommandResult;

public class EnrichTransferObjectOnPayloadCommand implements TransactionProcessorCommand {

    @Override
    public TransactionProcessorCommandResult execute(Transaction transaction) {

        Map<TransactionPayloadTypes, String> payload = transaction.getPayload();
        if (payload.containsKey(TransactionPayloadTypes.EDITABLE_TRANSACTION_TRANSFER) &&
                payload.containsKey(TransactionPayloadTypes.EDITABLE_TRANSACTION_TRANSFER_ID)) {

            String payloadValue = payload.get(TransactionPayloadTypes.EDITABLE_TRANSACTION_TRANSFER);
            Transfer transfer = SerializationUtils.deserializeFromString(payloadValue, Transfer.class);

            if (transfer != null) {
                // This is done so that GET /transfer/{id} can be done without having to search all transactions
                transfer.setId(UUIDUtils.fromTinkUUID(transaction.getId()));

                transfer.setUserId(UUIDUtils.fromTinkUUID(transaction.getUserId()));
                transfer.setCredentialsId(UUIDUtils.fromTinkUUID(transaction.getCredentialsId()));

                payload.put(TransactionPayloadTypes.EDITABLE_TRANSACTION_TRANSFER,
                        SerializationUtils.serializeToString(transfer));
                payload.put(TransactionPayloadTypes.EDITABLE_TRANSACTION_TRANSFER_ID,
                        UUIDUtils.toTinkUUID(transfer.getId()));

                transaction.setPayload(payload);
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
