package se.tink.backend.aggregation.workers;

import com.google.common.collect.Lists;
import java.util.List;
import se.tink.libraries.metrics.MetricId;

public abstract class AgentWorkerCommand {
    /**
     * Called for every command in a command chain. The chain of method invocations is broken unless
     * AgentWorkerCommandResult.CONTINUE
     *
     * @return whether an {@link AgentWorkerOperation} should continue running the other command's execute methods or
     * not.
     * @throws Exception on error
     */
    public abstract AgentWorkerCommandResult execute() throws Exception;

    /**
     * Called for every command in a command chain's reverse order.
     *
     * @throws Exception on error
     */
    public abstract void postProcess() throws Exception;

    public List<MetricId.MetricLabels> getCommandTimerName(AgentWorkerOperationMetricType type) {
        return Lists.newArrayList();
    }
}
