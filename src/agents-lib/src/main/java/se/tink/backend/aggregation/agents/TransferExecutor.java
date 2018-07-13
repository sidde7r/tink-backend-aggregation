package se.tink.backend.aggregation.agents;

import se.tink.backend.core.transfer.Transfer;

public interface TransferExecutor extends HttpLoggableExecutor {
    void execute(Transfer transfer) throws Exception, TransferExecutionException;
    void update(Transfer transfer) throws Exception, TransferExecutionException;
}
