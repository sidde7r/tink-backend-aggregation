package se.tink.backend.aggregation.workers.commands.login;

import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.workers.metrics.AgentWorkerCommandMetricState;
import se.tink.backend.aggregation.workers.metrics.MetricAction;
import se.tink.backend.aggregation.workers.metrics.MetricActionComposite;
import se.tink.backend.aggregation.workers.metrics.MetricActionIface;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.metrics.core.MetricId;
import src.libraries.interaction_counter.InteractionCounter;

@AllArgsConstructor
public class MetricsFactory {

    static class MetricName {
        static final String LOGIN = "login";
        static final String LOGIN_MANUAL = "login-manual";
        static final String LOGIN_AUTO = "login-auto";
        static final String LOGIN_CRON = "login-cron";
    }

    private final AgentWorkerCommandMetricState agentWorkerCommandMetricState;

    public MetricActionIface createLoginMetric(
            CredentialsRequest credentialsRequest,
            InteractionCounter supplementalInformationInteractionCounter) {
        MetricAction action =
                agentWorkerCommandMetricState.buildAction(metricForAction(MetricName.LOGIN));
        if (isBackgroundCronRefresh(credentialsRequest)) {
            return new MetricActionComposite(
                    action,
                    agentWorkerCommandMetricState.buildAction(
                            metricForAction(MetricName.LOGIN_CRON)));
        }
        int interactions = supplementalInformationInteractionCounter.getNumberInteractions();
        MetricAction actionLoginType =
                interactions == 0
                        ? agentWorkerCommandMetricState.buildAction(
                                metricForAction(MetricName.LOGIN_AUTO))
                        : agentWorkerCommandMetricState.buildAction(
                                metricForAction(MetricName.LOGIN_MANUAL));
        return new MetricActionComposite(action, actionLoginType);
    }

    private MetricId.MetricLabels metricForAction(String action) {
        return new MetricId.MetricLabels().add("action", action);
    }

    private boolean isBackgroundCronRefresh(CredentialsRequest credentialsRequest) {
        return !credentialsRequest.isManual();
    }
}
