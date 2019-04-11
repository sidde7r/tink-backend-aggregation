package se.tink.backend.aggregation.nxgen.framework.validation;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.agents.models.Transaction;
import se.tink.libraries.customerinfo.IdentityData;

public final class AisValidator {
    private final Set<AisDataRule> aisDataRules;
    private final Set<AccountRule> accountRules;
    private final Set<TransactionRule> transactionRules;

    private final ValidationExecutor executor;

    private AisValidator(final AisValidator.Builder builder) {
        aisDataRules = builder.aisDataRules;
        accountRules = builder.accountRules;
        transactionRules = builder.transactionRules;
        executor = builder.executor;
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
        builder.executor = executor;
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
            final Collection<Account> accounts,
            final Collection<Transaction> transactions,
            final IdentityData customerInfo) {
        validate(new AisData(accounts, transactions, customerInfo));
    }

    public void validate(final AisData aisData) {
        final ValidationResult result =
                ValidationResult.builder()
                        .addAisDataRules(aisDataRules)
                        .addAccountRules(accountRules)
                        .addTransactionRules(transactionRules)
                        .validate(aisData)
                        .build();

        executor.execute(result);
    }

    public static final class Builder {
        private Set<AisDataRule> aisDataRules = new HashSet<>();
        private Set<AccountRule> accountRules = new HashSet<>();
        private Set<TransactionRule> transactionRules = new HashSet<>();

        private ValidationExecutor executor;

        private Builder() {}

        public AisValidator build() {
            if (executor == null) {
                executor = new WarnExecutor();
            }
            return new AisValidator(this);
        }

        private <V> void checkForDuplicates(final ValidationRule<V> rule) {
            // TODO checked exception alternative
            final boolean accountDupes =
                    accountRules.stream()
                            .map(AccountRule::getRuleIdentifier)
                            .anyMatch(id -> Objects.equals(id, rule.getRuleIdentifier()));
            final boolean transactionDupes =
                    transactionRules.stream()
                            .map(TransactionRule::getRuleIdentifier)
                            .anyMatch(id -> Objects.equals(id, rule.getRuleIdentifier()));
            final boolean aisDataDupes =
                    aisDataRules.stream()
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

        /** Create a rule without supplying a fail message */
        public Builder rule(final String ruleName, final Predicate<AisData> criterion) {
            return rule(ruleName, criterion, aisData -> "");
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

        public Builder setExecutor(final ValidationExecutor executor) {
            this.executor = executor;
            return this;
        }

        public Builder disableRule(final String ruleIdentifier) {
            aisDataRules.removeIf(rule -> Objects.equals(rule.getRuleIdentifier(), ruleIdentifier));
            accountRules.removeIf(rule -> Objects.equals(rule.getRuleIdentifier(), ruleIdentifier));
            transactionRules.removeIf(
                    rule -> Objects.equals(rule.getRuleIdentifier(), ruleIdentifier));
            return this;
        }
    }
}
