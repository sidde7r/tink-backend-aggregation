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
    private final Set<TransactionRule> transactionRules;
    private final Action action;

    private AisValidator(final AisValidator.Builder builder) {
        rules = builder.rules;
        transactionRules = builder.transactionRules;
        action = builder.action;
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * @return A builder based on this instance. All mutable members from this instance are
     *     deep-copied into the builder.
     */
    public Builder rebuilder() {
        final Builder builder = new Builder();
        builder.rules = new HashSet<>(rules);
        builder.transactionRules = new HashSet<>(transactionRules);
        builder.action = action;
        return builder;
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
        for (final TransactionRule rule : transactionRules) {
            for (final Transaction transaction : aisData.getTransactions()) {
                validateWithTransactionRule(transaction, rule, action);
            }
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

    private static void validateWithTransactionRule(
            final Transaction transaction, final TransactionRule rule, final Action action) {
        if (rule.getCriterion().test(transaction)) {
            action.onPass(transaction, rule.getRuleIdentifier());
        } else {
            action.onFail(transaction, rule.getRuleIdentifier(), rule.getMessage(transaction));
        }
    }

    public static final class Builder {
        private Set<ValidationRule> rules = new HashSet<>();
        private Set<TransactionRule> transactionRules = new HashSet<>();
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

        public Builder ruleTransaction(
                final String ruleName,
                final Predicate<Transaction> criterion,
                final Function<Transaction, String> failMessage) {
            final TransactionRule rule = new TransactionRule(ruleName, criterion, failMessage);
            if (transactionRules.contains(rule)) {
                throw new IllegalArgumentException(
                        String.format(
                                "A validation rule with identifier \"%s\" was already specified",
                                rule.getRuleIdentifier()));
            }
            transactionRules.add(rule);
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
