package se.tink.backend.aggregation.agents.nxgen.framework.validation;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import se.tink.backend.aggregation.rpc.Account;
import se.tink.backend.system.rpc.Transaction;

public final class AisValidator {
    private final Set<ValidationRule> rules;
    private final Action action;

    private AisValidator(final AisValidator.Builder builder) {
        rules = builder.rules;
        action = builder.action;
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Carry out the actual validation. The validator's associated action will be executed and will
     * depend on the validation result.
     *
     * @param accounts A collection of accounts to be validated
     * @param transactions A collection of transactions to be validated
     */
    public void validate(
            final Collection<Account> accounts, final Collection<Transaction> transactions) {
        validate(new AisData(accounts, transactions));
    }

    public void validate(final AisData aisData) {
        for (final ValidationRule rule : rules) {
            validateWithRule(aisData, rule, action);
        }
    }

    private static void validateWithRule(
            final AisData aisData, final ValidationRule rule, final Action action) {
        if (rule.getAccountCriterion() != null) {
            for (final Account account : aisData.getAccounts()) {
                if (rule.getAccountCriterion().test(account)) {
                    action.onPass(account, rule.getRuleIdentifier());
                } else {
                    action.onFail(account, rule.getRuleIdentifier(), rule.getMessage(account));
                }
            }
        }
        if (rule.getAisDataCriterion() != null) {
            if (rule.getAisDataCriterion().test(aisData)) {
                action.onPass(aisData, rule.getRuleIdentifier());
            } else {
                action.onFail(aisData, rule.getRuleIdentifier(), rule.getMessage(aisData));
            }
        }
    }

    public static final class Builder {
        private Set<ValidationRule> rules = new HashSet<>();
        private Action action = null;

        private Builder() {}

        public AisValidator build() {
            if (action == null) {
                action = new WarnAction();
            }
            return new AisValidator(this);
        }

        /**
         * Add a rule to the validator.
         *
         * @param ruleName A human-readable string identifier for the rule
         * @param criterion If the object under validation does not satisfy this condition, it is
         *     invalid
         * @param failMessage Human-readable message describing, in the context of the AIS data, why
         *     the rule failed
         * @return Builder instance
         */
        public Builder rule(
                final String ruleName,
                final Predicate<AisData> criterion,
                final Function<AisData, String> failMessage) {
            rules.add(ValidationRule.fromAisData(ruleName, criterion, failMessage));
            return this;
        }

        public Builder ruleAccount(
                final String ruleName,
                final Predicate<Account> criterion,
                final Function<Account, String> failMessage) {

            final ValidationRule rule =
                    ValidationRule.fromAccount(ruleName, criterion, failMessage);
            // TODO checked exception alternative
            if (rules.contains(rule)) {
                throw new IllegalArgumentException(
                        String.format(
                                "A validation rule with identifier \"%s\" was already specified",
                                rule.getRuleIdentifier()));
            }
            rules.add(rule);
            return this;
        }

        public Builder ruleAccount(final String ruleName, final Predicate<Account> criterion) {
            return ruleAccount(ruleName, criterion, a -> "");
        }

        public Builder setAction(final Action action) {
            this.action = action;
            return this;
        }
    }
}
