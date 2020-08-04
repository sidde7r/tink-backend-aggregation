package se.tink.backend.aggregation.workers.agent_metrics;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import se.tink.backend.agents.rpc.CredentialsStatus;
import se.tink.backend.aggregation.configuration.models.ProviderTierConfiguration;
import se.tink.backend.aggregation.configuration.models.ProviderTierConfiguration.Tier;
import se.tink.backend.aggregation.rpc.TransferRequest;
import se.tink.backend.aggregation.workers.context.AgentWorkerCommandContext;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.metrics.core.MetricId;
import se.tink.libraries.metrics.core.MetricId.MetricLabels;
import se.tink.libraries.metrics.registry.MetricRegistry;
import se.tink.libraries.metrics.types.counters.Counter;
import se.tink.libraries.signableoperation.rpc.SignableOperation;

public class AgentWorkerMetricReporter {
    private final MetricRegistry registry;
    private final ProviderTierConfiguration providerTierConfiguration;

    private enum Metric {
        operationsTotal(MetricId.newId("aggregation_total_operations")),
        operationsFailed(MetricId.newId("aggregation_failed_operations")),
        operationsSuccessful(MetricId.newId("aggregation_successful_operations")),

        marketsTotal(MetricId.newId("aggregation_total_market")),
        marketsFailed(MetricId.newId("aggregation_failed_market")),
        marketsSuccessful(MetricId.newId("aggregation_successful_market")),

        marketsOperationsTotal(MetricId.newId("aggregation_total_market_operations")),
        marketsOperationsFailed(MetricId.newId("aggregation_failed_market_operations")),
        marketsOperationsSuccessful(MetricId.newId("aggregation_successful_market_operations")),

        tierMarketsTotal(MetricId.newId("aggregation_total_market")),
        tierMarketsFailed(MetricId.newId("aggregation_failed_market")),
        tierMarketsSuccessful(MetricId.newId("aggregation_successful_market")),

        tierProviderTotal(MetricId.newId("aggregation_total_market_tier")),
        tierProviderFailed(MetricId.newId("aggregation_failed_market_tier")),
        tierProviderSuccessful(MetricId.newId("aggregation_successful_market_tier"));

        private MetricId metricId;

        Metric(MetricId metric) {
            metricId = metric;
        }

        Counter get(MetricRegistry registry, MetricLabels labels) {
            return registry.meter(metricId.label(labels));
        }
    }

    private class MetricLabelPair {
        private final Metric metric;
        private final MetricLabels labels;

        private MetricLabelPair(Metric metric, MetricLabels labels) {
            this.metric = metric;
            this.labels = labels;
        }

        public MetricLabels getLabels() {
            return labels;
        }

        public Metric getMetric() {
            return metric;
        }
    }

    public AgentWorkerMetricReporter(
            MetricRegistry registry, ProviderTierConfiguration providerTierConfiguration) {
        this.registry = registry;
        this.providerTierConfiguration = providerTierConfiguration;
    }

    /*
    Although it might look weird to count "user errors" (such as cancelled/auth_error)
    as failed requests, we do this to ensure that we track anything that could potentially
    be concluded to be an error on the bank side or the tink side.

    This will cause our "success rate" to be lower than the KPI numbers will show, but as we want to
    track the statistics over a longer period of time to discern standard deviation, we only care
    that the number degrades to the point of the Z value being above 4.
     */
    private boolean isSuccessfulStatus(CredentialsStatus status) {
        switch (status) {
            case AUTHENTICATION_ERROR:
            case TEMPORARY_ERROR:
                return false;
            default:
                return true;
        }
    }

    private boolean isSuccessfulTransfer(TransferRequest request) {
        SignableOperation operation = request.getSignableOperation();
        switch (operation.getStatus()) {
            case CANCELLED:
            case FAILED:
                return false;
            default:
                return true;
        }
    }

    private boolean isSuccessful(AgentWorkerCommandContext context) {
        CredentialsRequest request = context.getRequest();
        CredentialsStatus status = request.getCredentials().getStatus();
        ArrayList<Boolean> successHints = new ArrayList<>();
        if (request instanceof TransferRequest) {
            successHints.add(isSuccessfulTransfer((TransferRequest) request));
        }
        successHints.add(isSuccessfulStatus(status));

        return successHints.stream().allMatch(val -> val);
    }

    private void reportMetrics(MetricLabels labels, Metric metric) {
        metric.get(registry, labels).inc();
    }

    private List<MetricLabelPair> operationMetrics(
            AgentWorkerCommandContext context, String operationName) {
        final List<MetricLabelPair> metricLabelPairs = new ArrayList<>();
        final MetricLabels marketOperationLabels =
                MetricLabels.from(
                        ImmutableMap.of(
                                "market",
                                context.getRequest().getProvider().getMarket(),
                                "operation",
                                operationName));
        final MetricLabels operationLabels =
                MetricLabels.from(ImmutableMap.of("operation", operationName));

        metricLabelPairs.add(
                new MetricLabelPair(Metric.marketsOperationsTotal, marketOperationLabels));
        metricLabelPairs.add(new MetricLabelPair(Metric.operationsTotal, operationLabels));

        metricLabelPairs.add(
                new MetricLabelPair(
                        isSuccessful(context)
                                ? Metric.marketsOperationsSuccessful
                                : Metric.marketsOperationsFailed,
                        marketOperationLabels));
        metricLabelPairs.add(
                new MetricLabelPair(
                        isSuccessful(context)
                                ? Metric.operationsSuccessful
                                : Metric.operationsFailed,
                        operationLabels));

        return metricLabelPairs;
    }

    private List<MetricLabelPair> marketMetrics(AgentWorkerCommandContext context) {
        final List<MetricLabelPair> metricLabelPairs = new ArrayList<>();
        final MetricLabels marketLabels =
                MetricLabels.from(
                        ImmutableMap.of("market", context.getRequest().getProvider().getMarket()));

        metricLabelPairs.add(new MetricLabelPair(Metric.marketsTotal, marketLabels));
        metricLabelPairs.add(
                new MetricLabelPair(
                        isSuccessful(context) ? Metric.marketsSuccessful : Metric.marketsFailed,
                        marketLabels));

        return metricLabelPairs;
    }

    private List<MetricLabelPair> tierMetrics(AgentWorkerCommandContext context) {
        final List<MetricLabelPair> metricLabelPairs = new ArrayList<>();
        final String providerName = context.getRequest().getProvider().getName();
        final String market = context.getRequest().getProvider().getMarket();
        final Tier tier =
                providerTierConfiguration.getTierForProvider(market, providerName).orElse(Tier.T3);
        final String tierName = tier.toString();

        final MetricLabels tierLabels =
                MetricLabels.from(
                        ImmutableMap.of(
                                "market",
                                context.getRequest().getProvider().getMarket(),
                                "tier",
                                tierName));

        final MetricLabels tierProviderLabel =
                MetricLabels.from(
                        ImmutableMap.of(
                                "market", context.getRequest().getProvider().getMarket(),
                                "tier", tierName,
                                "provider", providerName));

        metricLabelPairs.add(new MetricLabelPair(Metric.tierMarketsTotal, tierLabels));
        metricLabelPairs.add(
                new MetricLabelPair(
                        isSuccessful(context)
                                ? Metric.tierMarketsSuccessful
                                : Metric.tierMarketsFailed,
                        tierLabels));

        if (Objects.equals(tier, Tier.T1)) {
            metricLabelPairs.add(new MetricLabelPair(Metric.tierProviderTotal, tierProviderLabel));
            metricLabelPairs.add(
                    new MetricLabelPair(
                            isSuccessful(context)
                                    ? Metric.tierProviderSuccessful
                                    : Metric.tierProviderFailed,
                            tierProviderLabel));
        }

        return metricLabelPairs;
    }

    public void observe(AgentWorkerCommandContext context, String operationName) {
        ImmutableList<MetricLabelPair> metricLabelPairs =
                ImmutableList.<MetricLabelPair>builder()
                        .addAll(operationMetrics(context, operationName))
                        .addAll(marketMetrics(context))
                        .addAll(tierMetrics(context))
                        .build();

        for (MetricLabelPair metricLabelPair : metricLabelPairs) {
            reportMetrics(metricLabelPair.getLabels(), metricLabelPair.getMetric());
        }
    }
}
