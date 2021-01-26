package se.tink.backend.aggregation.nxgen.controllers.metrics;

import java.util.Collections;
import java.util.List;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregationcontroller.v1.rpc.enums.CredentialsRequestType;
import se.tink.libraries.metrics.core.MetricId;
import se.tink.libraries.metrics.registry.MetricRegistry;

public class MetricRefreshController extends MetricController {
    public MetricRefreshController(
            MetricRegistry registry,
            Provider provider,
            Credentials credentials,
            boolean isManual,
            CredentialsRequestType requestType) {
        super(registry, provider, credentials, isManual, requestType);
    }

    @Override
    public MetricRefreshAction buildAction(MetricId metricId) {
        return buildAction(metricId, Collections.emptyList());
    }

    public MetricRefreshAction buildAction(
            MetricId metricId, List<? extends Number> counterBuckets) {
        return new MetricRefreshAction(
                credentials, registry, metricId.label(defaultLabels), counterBuckets);
    }
}
