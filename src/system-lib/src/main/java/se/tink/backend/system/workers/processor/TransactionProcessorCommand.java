package se.tink.backend.system.workers.processor;

import se.tink.backend.core.Transaction;

public interface TransactionProcessorCommand {

    /**
     * Called once before any call to {@link #execute(Transaction)}.
     *
     * @return whether the command chain could continue to run or not.
     */
    TransactionProcessorCommandResult initialize();

    /**
     * Called once per transaction.
     *
     * @param transaction
     * @return whether the command chain could continue to run or not.
     */
    TransactionProcessorCommandResult execute(Transaction transaction);

    /**
     * Called for every command in command chain's reverse order at after processing all transactions.
     */
    void postProcess();
}
