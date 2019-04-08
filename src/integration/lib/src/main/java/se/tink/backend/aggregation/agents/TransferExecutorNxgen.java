package se.tink.backend.aggregation.agents;

import java.util.Optional;
import se.tink.libraries.transfer.rpc.Transfer;

public interface TransferExecutorNxgen extends HttpLoggableExecutor {
    Optional<String> execute(Transfer transfer);

    void update(Transfer transfer);
}
