package se.tink.backend.aggregation.workers.commands.login;

import java.util.Optional;
import se.tink.backend.aggregation.workers.metrics.MetricActionIface;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommandResult;

public interface LoginExceptionHandler {

    Optional<AgentWorkerCommandResult> handleLoginException(
            final Exception exception, final MetricActionIface metricAction);
}
