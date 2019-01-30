package se.tink.backend.aggregation.workers;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.inject.Inject;
import se.tink.backend.aggregation.agents.contexts.SystemUpdater;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.backend.agents.rpc.CredentialsStatus;
import se.tink.backend.aggregation.workers.metrics.TimerCacheLoader;
import se.tink.libraries.metrics.MetricId;
import se.tink.libraries.metrics.MetricRegistry;
import se.tink.libraries.metrics.Timer;
import se.tink.libraries.metrics.Timer.Context;
import se.tink.backend.agents.rpc.Credentials;

public class AgentWorkerOperation implements Runnable {
    public static class AgentWorkerOperationState {
        private LoadingCache<MetricId.MetricLabels, Timer> commandExecutionsTimers;

        @Inject
        public AgentWorkerOperationState(MetricRegistry metricRegistry) {
            commandExecutionsTimers = CacheBuilder.newBuilder().build(new TimerCacheLoader(
                    metricRegistry, "command_executions_duration"));
        }

        public LoadingCache<MetricId.MetricLabels, Timer> getCommandExecutionsTimers() {
            return commandExecutionsTimers;
        }
    }

    private static final AggregationLogger log = new AggregationLogger(AgentWorkerOperation.class);

    private List<AgentWorkerCommand> commands;
    private AgentWorkerContext context;
    private String operationMetricName;
    private CredentialsRequest request;
    private AgentWorkerOperationState state;
    private SystemUpdater systemUpdater;

    public AgentWorkerOperation(AgentWorkerOperationState state, String operationMetricName, CredentialsRequest request,
            List<AgentWorkerCommand> commands, AgentWorkerContext context) {
        this.operationMetricName = operationMetricName;
        this.request = request;
        this.commands = commands;
        this.context = context;
        this.systemUpdater = context;
        this.state = state;
    }

    public List<AgentWorkerCommand> getCommands() {
        return commands;
    }

    public AgentWorkerContext getContext() {
        return context;
    }

    public CredentialsRequest getRequest() {
        return request;
    }

    @Override
    public void run() {
        try {
            context.start();
        } catch (Exception e) {
            throw new RuntimeException("Could not start the agent worker context.", e);
        }
        try {
            executeAllCommands();
        } finally {
            try {
                context.stop();
            } catch (Exception e) {
                throw new RuntimeException("Could not throw the agent worker context.", e);
            }
        }
    }

    private void executeAllCommands() {
        Credentials credentials = request.getCredentials();

        log.info(String.format("Starting with command execution for operation '%s'",
                operationMetricName));

        AgentWorkerCommandResult commandResult = null;
        int currentCommand = 0;

        for (; currentCommand < commands.size(); currentCommand++) {
            AgentWorkerCommand command = commands.get(currentCommand);

            try {
                log.info(String.format("Executing command '%s' for operation '%s'",
                        command.toString(),
                        operationMetricName));

                List<Context> contexts = startCommandTimerContexts(command,
                        AgentWorkerOperationMetricType.EXECUTE_COMMAND);

                commandResult = command.execute();

                stopCommandContexts(contexts);

                if (commandResult == AgentWorkerCommandResult.ABORT) {
                    log.info(String.format("Got ABORT from command '%s' for operation '%s'",
                            command.toString(),
                            operationMetricName));

                    break;
                }

                if (Thread.interrupted()) {
                    log.info(String.format(
                            "Thread was interrupted when executing '%s' for operation '%s'. Aborting.",
                            command.toString(),
                            operationMetricName));

                    break;
                }

            } catch (Exception e) {
                log.error(String.format(
                        "Caught exception while executing command '%s' for operation '%s'",
                        command.toString(),
                        operationMetricName),
                        e);

                commandResult = AgentWorkerCommandResult.ABORT;

                // Set TEMPORARY_ERROR here on the credentials. If we catch an exception, we assume that the agent
                // didn't correctly set the status if it was an error that the agent could handle. If it handled it, it
                // should return ABORT.

                credentials.setStatus(CredentialsStatus.TEMPORARY_ERROR);
                credentials.setStatusPayload(null);
                systemUpdater.updateCredentialsExcludingSensitiveInformation(credentials, true);

                break;
            }
        }

        // Handle the status of the last executed command.

        if (commandResult == AgentWorkerCommandResult.CONTINUE) {
            log.info(String.format("Done with command execution for operation '%s'", operationMetricName));
        } else {
            log.info(String.format("Aborted command execution for operation '%s'", operationMetricName));
        }

        // Finalize all commands.

        int lastExecutedCommand = currentCommand - 1;

        for (currentCommand = commands.size() - 1; currentCommand >= 0; currentCommand--) {
            AgentWorkerCommand command = commands.get(currentCommand);

            try {
                log.info(String.format("Finalizing command '%s' for operation '%s'", command.toString(),
                        operationMetricName));

                List<Context> contexts = startCommandTimerContexts(command,
                        AgentWorkerOperationMetricType.POST_PROCESS_COMMAND);

                command.postProcess();

                stopCommandContexts(contexts);

            } catch (Exception e) {
                log.error(String.format(
                        "Caught exception while finalizing command '%s' for operation '%s'",
                        command.toString(),
                        operationMetricName),
                        e);
            }
        }

        log.info(String.format(
                "Done with command finalization for operation '%s'",
                operationMetricName));
    }

    private void stopCommandContexts(List<Context> contexts) {
        for (Context context : contexts) {
            context.stop();
        }
    }

    /**
     * Report both global and a credentials type specific metrics.
     */
    private List<Context> startCommandTimerContexts(AgentWorkerCommand command, AgentWorkerOperationMetricType type)
            throws ExecutionException {

        List<MetricId.MetricLabels> timerNames = command.getCommandTimerName(type);

        if (timerNames.size() == 0) {
            // Default
            timerNames.add(new MetricId.MetricLabels()
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
