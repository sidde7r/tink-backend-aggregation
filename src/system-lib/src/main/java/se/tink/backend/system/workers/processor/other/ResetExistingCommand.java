package se.tink.backend.system.workers.processor.other;

import com.google.common.base.MoreObjects;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.TransactionPayloadTypes;
import se.tink.backend.system.workers.processor.TransactionProcessorCommand;
import se.tink.backend.system.workers.processor.TransactionProcessorCommandResult;
import se.tink.backend.system.workers.processor.TransactionProcessorContext;

public class ResetExistingCommand implements TransactionProcessorCommand {

    private final boolean includeModifiedByUser;
    private final TransactionProcessorContext context;

    public ResetExistingCommand(TransactionProcessorContext context, boolean includeModifiedByUser) {
        this.context = context;
        this.includeModifiedByUser = includeModifiedByUser;
    }

    @Override
    public TransactionProcessorCommandResult initialize() {
        
        // reset stuff

        for (Transaction transaction : context.getInBatchTransactions()) {

            transaction.removePayload(TransactionPayloadTypes.TRANSFER_ACCOUNT);
            transaction.removePayload(TransactionPayloadTypes.TRANSFER_TWIN);
            
            if (!transaction.isUserModifiedCategory() || includeModifiedByUser) {
                transaction.resetCategory();
                transaction.setUserModifiedCategory(false);
            }

            if (!transaction.isUserModifiedDescription() || includeModifiedByUser) {
                transaction.setDescription(null);
            }
        }
        return TransactionProcessorCommandResult.CONTINUE;
    }

    @Override
    public TransactionProcessorCommandResult execute(Transaction transaction) {
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
