package se.tink.backend.aggregation.workers.metrics;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.workers.commands.metrics.MetricsCommand;
import se.tink.backend.aggregation.workers.operation.type.AgentWorkerOperationMetricType;
import se.tink.backend.aggregationcontroller.v1.rpc.enums.CredentialsRequestType;
import se.tink.libraries.metrics.core.MetricId;
import se.tink.libraries.metrics.registry.MetricRegistry;

public class AgentWorkerCommandMetricState {
    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final MetricRegistry metricRegistry;
    private final Provider provider;
    private final Credentials credentials;
    private MetricId.MetricLabels defaultLabels;
    private MetricsCommand command;
    private CredentialsRequestType requestType;

    private final List<MetricAction> actions = Lists.newArrayList();
    private MetricAction baseAction;

    public AgentWorkerCommandMetricState(
            Provider provider,
            Credentials credentials,
            MetricRegistry metricRegistry,
            CredentialsRequestType requestType) {
        this.provider = provider;
        this.credentials = credentials;
        this.metricRegistry = metricRegistry;
        this.requestType = requestType;
        this.defaultLabels = MetricId.MetricLabels.createEmpty();
    }

    public AgentWorkerCommandMetricState init(MetricsCommand command) {
        this.command = command;

        return this;
    }

    public AgentWorkerCommandMetricState init(
            MetricsCommand command, MetricId.MetricLabels labels) {
        defaultLabels = labels;
        return init(command);
    }

    private boolean isInitiated() {
        return command != null;
    }

    private boolean isOngoing() {
        return baseAction != null;
    }

    public void start(AgentWorkerOperationMetricType operationType) {
        Preconditions.checkState(
                isInitiated(),
                "Metrics state not initiated before starting, ensure the state is initiated inside the command constructor");
        Preconditions.checkState(!isOngoing(), "Metrics state already started");
        Preconditions.checkArgument(
                operationType != null,
                "Argument operationType is required when starting command metricCacheLoader state");

        baseAction =
                new MetricAction(
                        this,
                        metricRegistry,
                        MetricId.newId("agent_command")
                                .label("operation_type", operationType.getMetricName()));

        baseAction.start();
    }

    public void stop() {
        Preconditions.checkState(
                isInitiated(),
                "Metrics state not initiated, ensure the state is initiated inside the command constructor");
        Preconditions.checkState(
                isOngoing(),
                "Trying to stop AgentWorkerCommandMetricState without an ongoing MetricAction");

        baseAction.stop();

        if (!actions.isEmpty()) {
            stop(actions);
        }

        Preconditions.checkState(
                baseAction == null, String.format("Couldn't close baseAction: %s", baseAction));
    }

    /**
     * Removes a MetricAction from the list of ongoing actions
     *
     * <p>This method is called by the action itself when the action is stopped See:
     * MetricAction.stop()
     */
    void remove(MetricAction action) {
        if (baseAction == action) {
            baseAction = null;
        }

        if (actions.contains(action)) {
            actions.remove(action);
        }
    }

    /**
     * Adds a MetricAction to the list of ongoing actions
     *
     * <p>This method is called by the action itself when the action is started See:
     * MetricAction.start()
     */
    void add(MetricAction action) {
        Preconditions.checkArgument(action != null, "Cannot add null actions");
        Preconditions.checkState(
                !actions.contains(action), "Action already exists in the list of ongoing actions");

        if (!Objects.equals(action, baseAction)) {
            actions.add(action);
        }
    }

    /**
     * Make sure there are no more ongoing actions for the current command
     *
     * <p>There is no need to remove the action from the list of ongoing actions since the action
     * itself will call remove() automatically
     */
    private void stop(List<MetricAction> actions) {
        actions = Lists.newArrayList(actions);

        for (MetricAction action : actions) {
            logger.warn(String.format("Found unclosed MetricAction: %s", action.getActionName()));
            action.stop();
        }

        Preconditions.checkState(
                this.actions.isEmpty(),
                String.format("Couldn't close ongoing actions: %s", this.actions));
        throw new IllegalStateException(
                String.format("Found %s ongoing action timers", actions.size()));
    }

    public MetricAction buildAction(MetricId.MetricLabels action) {
        Preconditions.checkState(
                isInitiated(),
                "Metrics state not initiated, ensure the state is initiated inside the command constructor");

        return new MetricAction(
                this,
                metricRegistry,
                MetricId.newId(command.getMetricName())
                        .label(defaultLabels)
                        .label(action)
                        .label("provider_type", provider.getMetricTypeName())
                        .label("market", provider.getMarket())
                        .label("className", provider.getClassName())
                        .label("credential", credentials.getMetricTypeName())
                        .label("request_type", requestType.name()));
    }
}
