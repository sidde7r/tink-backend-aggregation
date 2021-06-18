package se.tink.backend.aggregation.nxgen.controllers.metrics;

import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregationcontroller.v1.rpc.enums.CredentialsRequestType;
import se.tink.libraries.credentials.service.UserAvailability;
import se.tink.libraries.metrics.core.MetricId;
import se.tink.libraries.metrics.registry.MetricRegistry;

public abstract class MetricController {
    final MetricRegistry registry;
    final MetricId.MetricLabels defaultLabels;
    final Credentials credentials;

    MetricController(
            MetricRegistry registry,
            Provider provider,
            Credentials credentials,
            UserAvailability userAvailability,
            CredentialsRequestType requestType) {
        this.registry = registry;
        this.defaultLabels =
                new MetricId.MetricLabels()
                        .add("provider_type", provider.getMetricTypeName())
                        .add("market", provider.getMarket())
                        .add("className", provider.getClassName())
                        .add("userPresent", String.valueOf(userAvailability.isUserPresent()))
                        .add(
                                "userAvailableForInteraction",
                                String.valueOf(userAvailability.isUserAvailableForInteraction()))
                        .add("credential", credentials.getMetricTypeName())
                        .add("request_type", requestType.name());
        this.credentials = credentials;
    }

    public abstract MetricAction buildAction(MetricId metricId);
}
