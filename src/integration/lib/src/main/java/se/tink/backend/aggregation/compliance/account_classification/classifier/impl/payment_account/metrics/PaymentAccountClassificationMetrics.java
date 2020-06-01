package se.tink.backend.aggregation.compliance.account_classification.classifier.impl.payment_account.metrics;

import se.tink.backend.agents.rpc.Account;
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

    public PaymentAccountClassificationMetrics(MetricRegistry metricRegistry) {
        this.metricRegistry = metricRegistry;
    }

    public void ruleResult(
            Provider provider,
            Account account,
            ClassificationRule<PaymentAccountClassification> rule,
            PaymentAccountClassification classificationResult) {
        metricRegistry
                .meter(
                        classificationRuleResult
                                .label("provider", provider.getName())
                                .label("market", provider.getMarket())
                                .label("account_type", account.getType().toString())
                                .label("rule", rule.getClass().getName())
                                .label("classification_result", classificationResult.toString()))
                .inc();
    }

    public void finalResult(
            Provider provider, Account account, PaymentAccountClassification classificationResult) {
        metricRegistry
                .meter(
                        finalClassificationResult
                                .label("provider", provider.getName())
                                .label("market", provider.getMarket())
                                .label("account_type", account.getType().toString())
                                .label("classification_result", classificationResult.toString()))
                .inc();
    }
}
