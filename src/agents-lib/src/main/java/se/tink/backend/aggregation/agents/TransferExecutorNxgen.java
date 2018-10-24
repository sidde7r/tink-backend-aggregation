package se.tink.backend.aggregation.agents;

import java.util.Optional;
import se.tink.backend.core.transfer.Transfer;

public interface TransferExecutorNxgen extends HttpLoggableExecutor {
    Optional<String> execute(Transfer transfer);
    void update(Transfer transfer);
}
