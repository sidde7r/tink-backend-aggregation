package se.tink.backend.aggregation.workers.operation;

import com.google.common.collect.Lists;
import java.util.List;
import org.slf4j.MDC;
import se.tink.backend.aggregation.workers.operation.type.AgentWorkerOperationMetricType;
import se.tink.libraries.metrics.core.MetricId;

@SuppressWarnings("squid:S112")
public abstract class AgentWorkerCommand {

    private static final String AGENT_WORKER_COMMAND_MDC_KEY = "command";
    /**
     * Called for every command in a command chain. The chain of method invocations is broken unless
     * AgentWorkerCommandResult.CONTINUE
     *
     * @return whether an {@link AgentWorkerOperation} should continue running the other command's
     *     execute methods or not.
     * @throws Exception on error
     */
    public final AgentWorkerCommandResult execute() throws Exception {
        MDC.put(AGENT_WORKER_COMMAND_MDC_KEY, getCommandName() + " execute");
        try {
            return doExecute();
        } finally {
            MDC.remove(AGENT_WORKER_COMMAND_MDC_KEY);
        }
    }

    protected abstract AgentWorkerCommandResult doExecute() throws Exception;

    /**
     * Called for every command in a command chain's reverse order.
     *
     * @throws Exception on error
     */
    public final void postProcess() throws Exception {
        MDC.put(AGENT_WORKER_COMMAND_MDC_KEY, getCommandName() + " postProcess");
        try {
            doPostProcess();
        } finally {
            MDC.remove(AGENT_WORKER_COMMAND_MDC_KEY);
        }
    }

    protected abstract void doPostProcess() throws Exception;

    /** Returns command human readable name, used for logs generation. */
    private String getCommandName() {
        return WorkerCommandNameFormatter.getCommandName(this.getClass());
    }

    public List<MetricId.MetricLabels> getCommandTimerName(AgentWorkerOperationMetricType type) {
        return Lists.newArrayList();
    }
}
