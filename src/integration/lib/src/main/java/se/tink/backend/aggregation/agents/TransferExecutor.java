package se.tink.backend.aggregation.agents;

import se.tink.libraries.transfer.rpc.Transfer;

public interface TransferExecutor extends HttpLoggableExecutor {
    void execute(Transfer transfer) throws Exception, TransferExecutionException;

    void update(Transfer transfer) throws Exception, TransferExecutionException;
}
