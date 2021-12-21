package se.tink.backend.aggregation.workers.commands.login;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.workers.metrics.AgentWorkerCommandMetricState;
import se.tink.backend.aggregation.workers.metrics.MetricAction;
import se.tink.backend.aggregation.workers.metrics.MetricActionComposite;
import se.tink.backend.aggregation.workers.metrics.MetricActionIface;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.metrics.core.MetricId;
import src.libraries.interaction_counter.InteractionCounter;

@AllArgsConstructor
@Slf4j
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
        MetricAction baseAction =
                agentWorkerCommandMetricState.buildAction(metricForAction(MetricName.LOGIN));
        String secondaryActionName;

        if (isBackgroundCronRefresh(credentialsRequest)) {
            secondaryActionName = MetricName.LOGIN_CRON;
        } else {
            secondaryActionName =
                    wasAnyUserInteraction(
                                    credentialsRequest, supplementalInformationInteractionCounter)
                            ? MetricName.LOGIN_MANUAL
                            : MetricName.LOGIN_AUTO;
        }
        log.info("Picked " + secondaryActionName + " as secondary login metric name.");
        return new MetricActionComposite(
                baseAction,
                agentWorkerCommandMetricState.buildAction(metricForAction(secondaryActionName)));
    }

    private boolean isBackgroundCronRefresh(CredentialsRequest credentialsRequest) {
        return !credentialsRequest.getUserAvailability().isUserPresent();
    }

    static boolean wasAnyUserInteraction(
            CredentialsRequest credentialsRequest,
            InteractionCounter supplementalInformationInteractionCounter) {
        int interactions = supplementalInformationInteractionCounter.getNumberInteractions();
        return credentialsRequest.isCreate()
                || credentialsRequest.isUpdate()
                || wasAuthenticationForced(credentialsRequest)
                || interactions > 0;
    }

    private static boolean wasAuthenticationForced(CredentialsRequest credentialsRequest) {
        return credentialsRequest.isForceAuthenticate()
                && credentialsRequest.getUserAvailability().isUserAvailableForInteraction();
    }

    private MetricId.MetricLabels metricForAction(String action) {
        return new MetricId.MetricLabels().add("action", action);
    }
}
