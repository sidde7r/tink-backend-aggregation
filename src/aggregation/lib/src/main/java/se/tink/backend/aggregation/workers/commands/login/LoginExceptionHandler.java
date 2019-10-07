package se.tink.backend.aggregation.workers.commands.login;

import java.util.Optional;
import se.tink.backend.aggregation.workers.AgentWorkerCommandResult;
import se.tink.backend.aggregation.workers.metrics.MetricActionIface;

public interface LoginExceptionHandler {

    Optional<AgentWorkerCommandResult> handleLoginException(
            final Exception exception, final MetricActionIface metricAction);
}
