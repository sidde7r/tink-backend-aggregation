package se.tink.backend.aggregation.compliance.account_classification.psd2_payment_account;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.compliance.account_classification.AccountClassificationMetrics;
import se.tink.backend.aggregation.compliance.account_classification.AccountClassificationRule;
import se.tink.backend.aggregation.compliance.account_classification.AccountClassifier;
import se.tink.backend.aggregation.compliance.account_classification.psd2_payment_account.result.Psd2PaymentAccountClassificationResult;
import se.tink.backend.aggregation.compliance.account_classification.psd2_payment_account.rules.common.CapabilitiesRule;
import se.tink.backend.aggregation.compliance.account_classification.psd2_payment_account.rules.common.CheckingAccountRule;
import se.tink.backend.aggregation.compliance.account_classification.psd2_payment_account.rules.market.UkCreditCardRule;
import se.tink.libraries.metrics.registry.MetricRegistry;

/**
 * This class determines whether an account classifies as PSD2 Payment Account, Non-Payment Account
 * or whether it was impossible to determine (UNDETERMINED).
 *
 * <p>Determination works as follows:
 *
 * <p>- if any applicable rule classifies the given account as PSD2 Payment Account then this is a
 * PSD2 Payment Account otherwise
 *
 * <p>- if any applicable rule that classifies the given account as PSD2 Non-Payment Account then
 * this is a PSD2 Non-Payment Account otherwise
 *
 * <p>- return PSD2 UNDETERMINED
 *
 * <p>Please note: no assumptions should be made on the order of rule evaluation
 */
public class Psd2PaymentAccountClassifier
        implements AccountClassifier<Psd2PaymentAccountClassificationResult> {
    private static final ImmutableList<
                    AccountClassificationRule<Psd2PaymentAccountClassificationResult>>
            defaultRules =
                    ImmutableList.of(
                            new CapabilitiesRule(),
                            new CheckingAccountRule(),

                            // Market specific rules:
                            new UkCreditCardRule());

    private final ImmutableList<AccountClassificationRule<Psd2PaymentAccountClassificationResult>>
            rules;

    private final @Nullable AccountClassificationMetrics<Psd2PaymentAccountClassificationResult>
            metrics;

    private Psd2PaymentAccountClassifier(
            @Nullable AccountClassificationMetrics<Psd2PaymentAccountClassificationResult> metrics,
            ImmutableList<AccountClassificationRule<Psd2PaymentAccountClassificationResult>>
                    rules) {
        this.rules = rules;
        this.metrics = metrics;
    }

    public static Psd2PaymentAccountClassifier createWithMetrics(MetricRegistry metricRegistry) {
        return new Psd2PaymentAccountClassifier(
                new AccountClassificationMetrics<>(metricRegistry), defaultRules);
    }

    public static Psd2PaymentAccountClassifier create() {
        return new Psd2PaymentAccountClassifier(null, defaultRules);
    }

    // Used in testing.
    static Psd2PaymentAccountClassifier createWithRules(
            ImmutableList<AccountClassificationRule<Psd2PaymentAccountClassificationResult>>
                    rules) {
        return new Psd2PaymentAccountClassifier(null, rules);
    }

    @Override
    public Optional<Psd2PaymentAccountClassificationResult> classify(
            Provider provider, Account account) {
        List<AccountClassificationRule<Psd2PaymentAccountClassificationResult>> applicableRules =
                getApplicableRules(provider);
        if (applicableRules.isEmpty()) {
            return Optional.empty();
        }

        List<Psd2PaymentAccountClassificationResult> allResults =
                collectClassificationResults(applicableRules, provider, account);

        Psd2PaymentAccountClassificationResult finalResult = pickResult(allResults);
        if (Objects.nonNull(metrics)) {
            metrics.finalResult(this, provider, account, finalResult);
        }

        return Optional.of(finalResult);
    }

    private List<AccountClassificationRule<Psd2PaymentAccountClassificationResult>>
            getApplicableRules(Provider provider) {
        return rules.stream()
                .filter(rule -> rule.isApplicable(provider))
                .collect(Collectors.toList());
    }

    private List<Psd2PaymentAccountClassificationResult> collectClassificationResults(
            List<AccountClassificationRule<Psd2PaymentAccountClassificationResult>> applicableRules,
            Provider provider,
            Account account) {
        return applicableRules.stream()
                .map(
                        rule -> {
                            Psd2PaymentAccountClassificationResult result =
                                    rule.classify(provider, account);
                            if (Objects.nonNull(metrics)) {
                                metrics.ruleResult(this, rule, provider, account, result);
                            }
                            return result;
                        })
                .collect(Collectors.toList());
    }

    private Psd2PaymentAccountClassificationResult pickResult(
            List<Psd2PaymentAccountClassificationResult> allResults) {
        if (anyMatch(allResults, Psd2PaymentAccountClassificationResult.PSD2_PAYMENT_ACCOUNT)) {
            return Psd2PaymentAccountClassificationResult.PSD2_PAYMENT_ACCOUNT;
        }
        if (anyMatch(allResults, Psd2PaymentAccountClassificationResult.PSD2_NON_PAYMENT_ACCOUNT)) {
            return Psd2PaymentAccountClassificationResult.PSD2_NON_PAYMENT_ACCOUNT;
        }

        return Psd2PaymentAccountClassificationResult.PSD2_UNDETERMINED_PAYMENT_ACCOUNT;
    }

    private boolean anyMatch(
            List<Psd2PaymentAccountClassificationResult> results,
            Psd2PaymentAccountClassificationResult expectedClassification) {
        return results.stream().anyMatch(result -> result == expectedClassification);
    }
}
