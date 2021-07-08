package se.tink.backend.aggregation.workers.abort;

import java.util.Optional;
import se.tink.backend.aggregation.workers.operation.OperationStatus;

public interface OperationAbortHandler {
    Optional<OperationStatus> handle(String operationId);
}
