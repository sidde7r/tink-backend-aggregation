package se.tink.backend.aggregation.workers.abort;

import java.util.Optional;
import se.tink.backend.aggregation.workers.operation.RequestStatus;

public interface RequestAbortHandler {
    Optional<RequestStatus> handle(String requestId);
}
