package se.tink.backend.aggregation.agents.payments;

import se.tink.backend.aggregation.agents.capability.CapabilityExecutor;
import se.tink.backend.aggregation.agents.exceptions.transfer.TransferExecutionException;
import se.tink.libraries.transfer.rpc.Transfer;

public interface TransferExecutor extends CapabilityExecutor {
    void execute(Transfer transfer) throws Exception, TransferExecutionException;
}
