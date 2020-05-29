package se.tink.backend.aggregation.compliance.account_classification.metrics;

import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.compliance.account_classification.PaymentAccountClassification;
import se.tink.backend.aggregation.compliance.account_classification.classifier.impl.ClassificationRule;
import se.tink.libraries.metrics.core.MetricId;
import se.tink.libraries.metrics.registry.MetricRegistry;

public class PaymentAccountClassificationMetrics {
    private static final MetricId classificationRule =
            MetricId.newId("aggregation_payment_account_classification_rule");
    private static final MetricId classificationDecision =
            MetricId.newId("aggregation_payment_account_classification_decision");

    private final MetricRegistry metricRegistry;

    public PaymentAccountClassificationMetrics(MetricRegistry metricRegistry) {
        this.metricRegistry = metricRegistry;
    }

    public void ruleResult(
            ClassificationRule<PaymentAccountClassification> rule,
            PaymentAccountClassification classificationResult) {
        metricRegistry
                .meter(
                        classificationRule
                                .label("rule", rule.getClass().getName())
                                .label("classification_result", classificationResult.toString()))
                .inc();
    }

    public void finalResult(PaymentAccountClassification classificationResult, Provider provider) {
        metricRegistry
                .meter(
                        classificationDecision
                                .label("provider", provider.getName())
                                .label("classification_result", classificationResult.toString()))
                .inc();
    }
}
