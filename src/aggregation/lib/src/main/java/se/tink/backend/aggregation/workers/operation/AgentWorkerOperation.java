package se.tink.backend.aggregation.workers.operation;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.ExecutionException;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import se.tink.backend.aggregation.agents.contexts.StatusUpdater;
import se.tink.backend.aggregation.workers.metrics.TimerCacheLoader;
import se.tink.backend.aggregation.workers.operation.type.AgentWorkerOperationMetricType;
import se.tink.backend.aggregationcontroller.v1.rpc.enums.CredentialsStatus;
import se.tink.connectivity.errors.ConnectivityError;
import se.tink.connectivity.errors.ConnectivityErrorDetails;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.metrics.core.MetricId;
import se.tink.libraries.metrics.registry.MetricRegistry;
import se.tink.libraries.metrics.types.timers.Timer;
import se.tink.libraries.metrics.types.timers.Timer.Context;
import src.libraries.connectivity_errors.ConnectivityErrorFactory;

@Slf4j
public class AgentWorkerOperation implements Runnable {
    public static class AgentWorkerOperationState {
        private LoadingCache<MetricId.MetricLabels, Timer> commandExecutionsTimers;

        @Inject
        public AgentWorkerOperationState(MetricRegistry metricRegistry) {
            commandExecutionsTimers =
                    CacheBuilder.newBuilder()
                            .build(
                                    new TimerCacheLoader(
                                            metricRegistry, "command_executions_duration"));
        }

        public LoadingCache<MetricId.MetricLabels, Timer> getCommandExecutionsTimers() {
            return commandExecutionsTimers;
        }
    }

    private static final String AGENT_WORKER_OPERATION_MDC_KEY = "operation";

    private List<AgentWorkerCommand> commands;
    private AgentWorkerContext context;
    private String operationMetricName;
    private CredentialsRequest request;
    private AgentWorkerOperationState state;
    private StatusUpdater statusUpdater;

    public AgentWorkerOperation(
            AgentWorkerOperationState state,
            String operationMetricName,
            CredentialsRequest request,
            List<AgentWorkerCommand> commands,
            AgentWorkerContext context) {
        this.operationMetricName = operationMetricName;
        this.request = request;
        this.commands = commands;
        this.context = context;
        this.statusUpdater = context;
        this.state = state;
    }

    public AgentWorkerContext getContext() {
        return context;
    }

    public CredentialsRequest getRequest() {
        return request;
    }

    @Override
    public void run() {
        MDC.put(AGENT_WORKER_OPERATION_MDC_KEY, this.operationMetricName);
        try {
            internalRun();
        } finally {
            MDC.remove(AGENT_WORKER_OPERATION_MDC_KEY);
        }
    }

    private void internalRun() {
        try {
            context.start();
            try {
                executeAllCommands();
            } finally {
                context.stop();
            }
        } catch (Exception ex) {
            throw new DropwizardManagedContextException(ex);
        }
    }

    private void executeAllCommands() {
        log.debug(
                "[AGENT WORKER OPERATION] Starting with command execution for operation '{}'",
                operationMetricName);

        AgentWorkerCommandResult commandResult = null;
        Stack<AgentWorkerCommand> executedCommands = new Stack<>();

        for (AgentWorkerCommand command : commands) {
            try {
                log.debug(
                        "[AGENT WORKER OPERATION] Executing command '{}' for operation '{}'",
                        command.getClass().getCanonicalName(),
                        operationMetricName);

                List<Context> contexts =
                        startCommandTimerContexts(
                                command, AgentWorkerOperationMetricType.EXECUTE_COMMAND);

                commandResult = command.execute();

                stopCommandContexts(contexts);

                if (commandResult == AgentWorkerCommandResult.ABORT) {
                    log.debug(
                            "[AGENT WORKER OPERATION] Got ABORT from command '{}' for operation '{}'",
                            command.getClass().getCanonicalName(),
                            operationMetricName);
                    break;
                }

                if (commandResult == AgentWorkerCommandResult.REJECT) {
                    log.debug(
                            "[AGENT WORKER OPERATION] Got REJECT from command '{}' for operation '{}'",
                            command.getClass().getCanonicalName(),
                            operationMetricName);
                    break;
                }

                if (Thread.interrupted()) {
                    log.debug(
                            "[AGENT WORKER OPERATION] Thread was interrupted when executing '{}' for operation '{}'. Aborting.",
                            command.getClass().getCanonicalName(),
                            operationMetricName);
                    break;
                }

            } catch (Exception e) {
                log.error(
                        "[AGENT WORKER OPERATION] Caught exception while executing command '{}' for operation '{}'",
                        command.getClass().getCanonicalName(),
                        operationMetricName,
                        e);

                commandResult = AgentWorkerCommandResult.ABORT;

                ConnectivityError error =
                        ConnectivityErrorFactory.tinkSideError(
                                ConnectivityErrorDetails.TinkSideErrors.TINK_INTERNAL_SERVER_ERROR);
                statusUpdater.updateStatusWithError(CredentialsStatus.TEMPORARY_ERROR, null, error);
                break;
            } finally {
                executedCommands.push(command);
            }
        }

        // Handle the status of the last executed command.

        if (commandResult == AgentWorkerCommandResult.CONTINUE) {
            log.debug(
                    "[AGENT WORKER OPERATION] Done with command execution for operation '{}'",
                    operationMetricName);
        }

        if (commandResult == AgentWorkerCommandResult.ABORT) {
            log.debug(
                    "[AGENT WORKER OPERATION] Aborted command execution for operation '{}'",
                    operationMetricName);
        }

        if (commandResult == AgentWorkerCommandResult.REJECT) {
            log.debug(
                    "[AGENT WORKER OPERATION] Rejected command execution for operation '{}'",
                    operationMetricName);
            // At the time of writing this comment, it can only occur if we fail to acquire lock
            ConnectivityError error =
                    ConnectivityErrorFactory.tinkSideError(
                            ConnectivityErrorDetails.TinkSideErrors.TINK_INTERNAL_SERVER_ERROR);
            statusUpdater.updateStatusWithError(CredentialsStatus.UNCHANGED, null, error);
        }

        // Finalize executed commands
        while (!executedCommands.isEmpty()) {
            AgentWorkerCommand command = executedCommands.pop();
            try {
                log.debug(
                        "[AGENT WORKER OPERATION] Starting post processing of command '{}' for operation '{}'",
                        command.getClass().getCanonicalName(),
                        operationMetricName);

                List<Context> contexts =
                        startCommandTimerContexts(
                                command, AgentWorkerOperationMetricType.POST_PROCESS_COMMAND);

                command.postProcess();

                stopCommandContexts(contexts);

            } catch (Exception e) {
                log.error(
                        "[AGENT WORKER OPERATION] Caught exception while finalizing command '{}' for operation '{}'",
                        command.getClass().getCanonicalName(),
                        operationMetricName,
                        e);
            }
        }

        log.info(
                "[AGENT WORKER OPERATION] Done with command finalization for operation '{}'",
                operationMetricName);
    }

    private void stopCommandContexts(List<Context> contexts) {
        for (Context context : contexts) {
            context.stop();
        }
    }

    /** Report both global and a credentials type specific metrics. */
    private List<Context> startCommandTimerContexts(
            AgentWorkerCommand command, AgentWorkerOperationMetricType type)
            throws ExecutionException {

        List<MetricId.MetricLabels> timerNames = command.getCommandTimerName(type);

        if (timerNames.size() == 0) {
            // Default
            timerNames.add(
                    new MetricId.MetricLabels()
                            .add("class", command.getClass().getSimpleName())
                            .add("command", type.getMetricName()));
        }

        List<Context> contexts = Lists.newArrayList();

        for (MetricId.MetricLabels timerName : timerNames) {
            contexts.add(getTimerContext(timerName));
        }

        return contexts;
    }

    private Context getTimerContext(MetricId.MetricLabels name) throws ExecutionException {
        return this.state.getCommandExecutionsTimers().get(name).time();
    }
}
