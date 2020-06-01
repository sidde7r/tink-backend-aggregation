package se.tink.backend.aggregation.compliance.account_classification.classifier.impl.payment_account;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.compliance.account_classification.PaymentAccountClassification;
import se.tink.backend.aggregation.compliance.account_classification.classifier.impl.ClassificationRule;
import se.tink.backend.aggregation.compliance.account_classification.classifier.impl.payment_account.metrics.PaymentAccountClassificationMetrics;
import se.tink.libraries.metrics.registry.MetricRegistry;

/**
 * This class determines whether an account classifies as Payment Account, Non-Payment Account or
 * whether it was impossible to determine (UNDETERMINED).
 *
 * <p>Determination works as follows:
 *
 * <p>- if any applicable rule classifies the given account as Payment Account then this is a
 * Payment Account otherwise
 *
 * <p>- if any applicable rule that classifies the given account as Non-Payment Account then this is
 * a Non-Payment Account otherwise
 *
 * <p>- return UNDETERMINED
 *
 * <p>Please note: no assumptions should be made on the order of rule evaluation
 */
public class PaymentAccountClassifier {
    private final List<ClassificationRule<PaymentAccountClassification>> rules;
    private final PaymentAccountClassificationMetrics metrics;

    public PaymentAccountClassifier(
            List<ClassificationRule<PaymentAccountClassification>> rules,
            MetricRegistry metricRegistry) {
        this.rules = rules;
        this.metrics = new PaymentAccountClassificationMetrics(metricRegistry);
    }

    public PaymentAccountClassification classifyAsPaymentAccount(
            Provider provider, Account account) {
        Stream<ClassificationRule<PaymentAccountClassification>> applicableRules =
                getApplicableRules(provider);
        List<PaymentAccountClassification> allResults =
                collectClassificationResults(provider, account, applicableRules);

        PaymentAccountClassification classificationResult = classify(allResults);
        metrics.finalResult(provider, account, classificationResult);
        return classificationResult;
    }

    private PaymentAccountClassification classify(List<PaymentAccountClassification> allResults) {
        if (anyMatch(allResults, PaymentAccountClassification.PAYMENT_ACCOUNT)) {
            return PaymentAccountClassification.PAYMENT_ACCOUNT;
        }
        if (anyMatch(allResults, PaymentAccountClassification.NON_PAYMENT_ACCOUNT)) {
            return PaymentAccountClassification.NON_PAYMENT_ACCOUNT;
        }

        return PaymentAccountClassification.UNDETERMINED;
    }

    private List<PaymentAccountClassification> collectClassificationResults(
            Provider provider,
            Account account,
            Stream<ClassificationRule<PaymentAccountClassification>> applicableRules) {
        return applicableRules
                .map(
                        rule -> {
                            PaymentAccountClassification result = rule.classify(provider, account);
                            metrics.ruleResult(provider, account, rule, result);
                            return result;
                        })
                .collect(Collectors.toList());
    }

    private Stream<ClassificationRule<PaymentAccountClassification>> getApplicableRules(
            Provider provider) {
        return Optional.ofNullable(rules).orElse(Collections.emptyList()).stream()
                .filter(rule -> rule.isApplicable(provider));
    }

    private boolean anyMatch(
            List<PaymentAccountClassification> results,
            PaymentAccountClassification expectedClassification) {
        return results.stream().anyMatch(result -> result == expectedClassification);
    }
}
