package se.tink.backend.aggregation.compliance.account_classification;

import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.Provider;
import se.tink.libraries.metrics.core.MetricId;
import se.tink.libraries.metrics.registry.MetricRegistry;

public class AccountClassificationMetrics<ClassificationResult> {
    private static final MetricId accountClassificationRule =
            MetricId.newId("aggregation_account_classification_rule");
    private static final MetricId accountClassificationResult =
            MetricId.newId("aggregation_account_classification_decision");

    private final MetricRegistry metricRegistry;

    public AccountClassificationMetrics(MetricRegistry metricRegistry) {
        this.metricRegistry = metricRegistry;
    }

    public void ruleResult(
            AccountClassifier<ClassificationResult> classifier,
            AccountClassificationRule<ClassificationResult> rule,
            Provider provider,
            Account account,
            ClassificationResult classificationResult) {
        metricRegistry
                .meter(
                        accountClassificationRule
                                .label("provider", provider.getName())
                                .label("market", provider.getMarket())
                                .label("account_type", account.getType().toString())
                                .label("classifier", classifier.getClass().getSimpleName())
                                .label("rule", rule.getClass().getSimpleName())
                                .label("classification_result", classificationResult.toString()))
                .inc();
    }

    public void finalResult(
            AccountClassifier<ClassificationResult> classifier,
            Provider provider,
            Account account,
            ClassificationResult classificationResult) {
        metricRegistry
                .meter(
                        accountClassificationResult
                                .label("provider", provider.getName())
                                .label("market", provider.getMarket())
                                .label("account_type", account.getType().toString())
                                .label("classifier", classifier.getClass().getSimpleName())
                                .label("classification_result", classificationResult.toString()))
                .inc();
    }
}
