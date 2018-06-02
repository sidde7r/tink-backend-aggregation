package se.tink.backend.aggregation.agents;

import se.tink.backend.core.transfer.Transfer;

public interface TransferExecutorNxgen extends HttpLoggableExecutor {
    void execute(Transfer transfer);
    void update(Transfer transfer);
}
