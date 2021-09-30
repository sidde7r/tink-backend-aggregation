package se.tink.backend.aggregation.workers.abort;

import com.google.inject.Inject;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.workers.operation.RequestStatus;
import se.tink.backend.aggregation.workers.operation.RequestStatusManager;

@Slf4j
public class DefaultRequestAbortHandler implements RequestAbortHandler {

    private final RequestStatusManager statusManager;

    @Inject
    public DefaultRequestAbortHandler(RequestStatusManager statusManager) {
        this.statusManager = statusManager;
    }

    @Override
    public Optional<RequestStatus> handle(String credentialsId) {
        Optional<RequestStatus> optionalStatus = statusManager.getByCredentialsId(credentialsId);
        if (!optionalStatus.isPresent()) {
            return Optional.empty();
        }

        RequestStatus status = optionalStatus.get();
        if (status != RequestStatus.STARTED) {
            return Optional.of(status);
        }

        boolean set =
                statusManager.compareAndSetByCredentialsId(
                        credentialsId, RequestStatus.STARTED, RequestStatus.TRYING_TO_ABORT);
        if (set) {
            return Optional.of(RequestStatus.TRYING_TO_ABORT);
        }

        return statusManager.getByCredentialsId(credentialsId);
    }
}
