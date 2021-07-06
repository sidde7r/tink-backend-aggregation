package se.tink.backend.aggregation.workers.operation;

import com.google.common.collect.Lists;
import io.opentracing.Span;
import io.opentracing.Tracer;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import se.tink.backend.aggregation.workers.operation.type.AgentWorkerOperationMetricType;
import se.tink.libraries.metrics.core.MetricId;
import se.tink.libraries.tracing.lib.api.Tracing;

@SuppressWarnings("squid:S112")
public abstract class AgentWorkerCommand {

    private static final Logger logger = LoggerFactory.getLogger(AgentWorkerCommand.class);

    private static final String AGENT_WORKER_COMMAND_MDC_KEY = "command";
    private Span spanForWorkerCommand;

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
            startSpanForJaeger("execute");
            return doExecute();
        } finally {
            MDC.remove(AGENT_WORKER_COMMAND_MDC_KEY);
            endSpanForJaeger("execute");
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
            startSpanForJaeger("postProcess");
            doPostProcess();
        } finally {
            MDC.remove(AGENT_WORKER_COMMAND_MDC_KEY);
            endSpanForJaeger("postProcess");
        }
    }

    private void startSpanForJaeger(String caller) {
        try {
            Tracer tracer = Tracing.getTracer();
            Span span = tracer.activeSpan();
            spanForWorkerCommand =
                    tracer.buildSpan(getCommandName() + "-" + caller).asChildOf(span).start();
        } catch (Exception e) {
            logger.warn("Could not set span for worker command for {}", caller);
        }
    }

    private void endSpanForJaeger(String caller) {
        try {
            spanForWorkerCommand.finish();
        } catch (Exception e) {
            logger.warn("Could not finish span for worker command for {}", caller);
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
