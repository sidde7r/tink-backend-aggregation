package se.tink.backend.aggregation.compliance.account_classification.metrics;

import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.compliance.account_classification.PaymentAccountClassification;
import se.tink.backend.aggregation.compliance.account_classification.classifier.impl.ClassificationRule;
import se.tink.libraries.metrics.core.MetricId;
import se.tink.libraries.metrics.registry.MetricRegistry;

public class PaymentAccountClassificationMetrics {
    private static final MetricId classificationRuleResult =
            MetricId.newId("aggregation_payment_account_classification_rule");
    private static final MetricId finalClassificationResult =
            MetricId.newId("aggregation_payment_account_classification_decision");

    private final MetricRegistry metricRegistry;
    private final MetricId.MetricLabels defaultMetricLabels;

    public PaymentAccountClassificationMetrics(MetricRegistry metricRegistry, Provider provider) {
        this.metricRegistry = metricRegistry;
        this.defaultMetricLabels =
                new MetricId.MetricLabels()
                        .add("provider", provider.getName())
                        .add("market", provider.getMarket());
    }

    public void ruleResult(
            ClassificationRule<PaymentAccountClassification> rule,
            PaymentAccountClassification classificationResult) {
        metricRegistry
                .meter(
                        classificationRuleResult
                                .label(defaultMetricLabels)
                                .label("rule", rule.getClass().getName())
                                .label("classification_result", classificationResult.toString()))
                .inc();
    }

    public void finalResult(PaymentAccountClassification classificationResult) {
        metricRegistry
                .meter(
                        finalClassificationResult
                                .label(defaultMetricLabels)
                                .label("classification_result", classificationResult.toString()))
                .inc();
    }
}
