package se.tink.backend.aggregation.workers.abort;

import com.google.inject.Inject;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.workers.operation.OperationStatus;
import se.tink.backend.aggregation.workers.operation.OperationStatusManager;

@Slf4j
public class DefaultOperationAbortHandler implements OperationAbortHandler {

    private final OperationStatusManager statusManager;

    @Inject
    public DefaultOperationAbortHandler(OperationStatusManager statusManager) {
        this.statusManager = statusManager;
    }

    @Override
    public Optional<OperationStatus> handle(String operationId) {
        Optional<OperationStatus> optionalStatus = statusManager.get(operationId);
        if (!optionalStatus.isPresent()) {
            return Optional.empty();
        }

        OperationStatus status = optionalStatus.get();
        if (status != OperationStatus.STARTED) {
            return Optional.of(status);
        }

        boolean set =
                statusManager.compareAndSet(
                        operationId, OperationStatus.STARTED, OperationStatus.TRYING_TO_ABORT);
        if (set) {
            return Optional.of(OperationStatus.TRYING_TO_ABORT);
        }

        return statusManager.get(operationId);
    }
}
