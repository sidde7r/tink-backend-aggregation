package se.tink.backend.aggregation.agents.nxgen.framework.validation;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import se.tink.backend.aggregation.rpc.Account;
import se.tink.backend.system.rpc.Transaction;

public final class AisValidator {
    private final Set<AisDataRule> aisDataRules;
    private final Set<AccountRule> accountRules;
    private final Set<TransactionRule> transactionRules;
    private final Action action;

    private AisValidator(final AisValidator.Builder builder) {
        aisDataRules = builder.aisDataRules;
        accountRules = builder.accountRules;
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
        builder.aisDataRules = new HashSet<>(aisDataRules);
        builder.accountRules = new HashSet<>(accountRules);
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
        for (final AisDataRule rule : aisDataRules) {
            validateWithRule(aisData, rule, action);
        }
        for (final AccountRule rule : accountRules) {
            for (final Account account : aisData.getAccounts()) {
                validateWithAccountRule(account, rule, action);
            }
        }
        for (final TransactionRule rule : transactionRules) {
            for (final Transaction transaction : aisData.getTransactions()) {
                validateWithTransactionRule(transaction, rule, action);
            }
        }
    }

    private static void validateWithRule(
            final AisData aisData, final AisDataRule rule, final Action action) {
        if (rule.getCriterion().test(aisData)) {
            action.onPass(aisData, rule.getRuleIdentifier());
        } else {
            action.onFail(aisData, rule.getRuleIdentifier(), rule.getMessage(aisData));
        }
    }

    private static void validateWithAccountRule(
            final Account account, final AccountRule rule, final Action action) {
        if (rule.getCriterion().test(account)) {
            action.onPass(account, rule.getRuleIdentifier());
        } else {
            action.onFail(account, rule.getRuleIdentifier(), rule.getMessage(account));
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
        private Set<AisDataRule> aisDataRules = new HashSet<>();
        private Set<AccountRule> accountRules = new HashSet<>();
        private Set<TransactionRule> transactionRules = new HashSet<>();
        private Action action = null;

        private Builder() {}

        public AisValidator build() {
            if (action == null) {
                action = new WarnAction();
            }
            return new AisValidator(this);
        }

        private <V> void checkForDuplicates(final ValidationRule<V> rule) {
            // TODO checked exception alternative
            final boolean accountDupes =
                    accountRules
                            .stream()
                            .map(AccountRule::getRuleIdentifier)
                            .anyMatch(id -> Objects.equals(id, rule.getRuleIdentifier()));
            final boolean transactionDupes =
                    transactionRules
                            .stream()
                            .map(TransactionRule::getRuleIdentifier)
                            .anyMatch(id -> Objects.equals(id, rule.getRuleIdentifier()));
            final boolean aisDataDupes =
                    aisDataRules
                            .stream()
                            .map(AisDataRule::getRuleIdentifier)
                            .anyMatch(id -> Objects.equals(id, rule.getRuleIdentifier()));
            if (aisDataDupes || accountDupes || transactionDupes) {
                throw new IllegalArgumentException(
                        String.format(
                                "A validation rule with identifier \"%s\" was already specified",
                                rule.getRuleIdentifier()));
            }
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
            final AisDataRule rule = new AisDataRule(ruleName, criterion, failMessage);
            checkForDuplicates(rule);
            aisDataRules.add(new AisDataRule(ruleName, criterion, failMessage));
            return this;
        }

        public Builder ruleAccount(
                final String ruleName,
                final Predicate<Account> criterion,
                final Function<Account, String> failMessage) {

            final AccountRule rule = new AccountRule(ruleName, criterion, failMessage);
            checkForDuplicates(rule);
            accountRules.add(rule);
            return this;
        }

        /** Create a rule without supplying a fail message */
        public Builder ruleAccount(final String ruleName, final Predicate<Account> criterion) {
            return ruleAccount(ruleName, criterion, a -> "");
        }

        public Builder ruleTransaction(
                final String ruleName,
                final Predicate<Transaction> criterion,
                final Function<Transaction, String> failMessage) {
            final TransactionRule rule = new TransactionRule(ruleName, criterion, failMessage);
            checkForDuplicates(rule);
            transactionRules.add(rule);
            return this;
        }

        /** Create a rule without supplying a fail message */
        public Builder ruleTransaction(
                final String ruleName, final Predicate<Transaction> criterion) {
            return ruleTransaction(ruleName, criterion, a -> "");
        }

        public Builder setAction(final Action action) {
            this.action = action;
            return this;
        }

        public Builder disableRule(final String ruleIdentifier) {
            aisDataRules.removeIf(rule -> Objects.equals(rule.getRuleIdentifier(), ruleIdentifier));
            accountRules.removeIf(rule -> Objects.equals(rule.getRuleIdentifier(), ruleIdentifier));
            transactionRules.removeIf(rule -> Objects.equals(rule.getRuleIdentifier(), ruleIdentifier));
            return this;
        }
    }
}
