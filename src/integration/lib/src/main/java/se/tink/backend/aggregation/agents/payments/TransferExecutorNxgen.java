package se.tink.backend.aggregation.agents.payments;

import java.util.Optional;
import se.tink.backend.aggregation.agents.capability.CapabilityExecutor;
import se.tink.libraries.transfer.rpc.Transfer;

public interface TransferExecutorNxgen extends CapabilityExecutor {
    Optional<String> execute(Transfer transfer);
}
