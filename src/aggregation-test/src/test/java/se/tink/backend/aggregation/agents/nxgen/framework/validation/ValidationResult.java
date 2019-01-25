package se.tink.backend.aggregation.agents.nxgen.framework.validation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import se.tink.backend.aggregation.rpc.Account;
import se.tink.backend.system.rpc.Transaction;

/** The result of validating the set of criteria. */
public final class ValidationResult {
    private final Map<String, ValidationSubResult> subResults;

    private final Set<AisDataRule> aisDataRules;
    private final Set<AccountRule> accountRules;
    private final Set<TransactionRule> transactionRules;

    private ValidationResult(final Builder builder) {
        aisDataRules = builder.aisDataRules;
        accountRules = builder.accountRules;
        transactionRules = builder.transactionRules;
        subResults = builder.subResults;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Map<String, ValidationSubResult> getSubResults() {
        return subResults;
    }

    public static class Builder {
        private Set<AisDataRule> aisDataRules = new HashSet<>();
        private Set<AccountRule> accountRules = new HashSet<>();
        private Set<TransactionRule> transactionRules = new HashSet<>();

        private Map<String, ValidationSubResult> subResults = new HashMap<>();

        public ValidationResult build() {
            return new ValidationResult(this);
        }

        public Builder addAisDataRules(final Set<AisDataRule> rules) {
            aisDataRules = rules;
            return this;
        }

        public Builder addAccountRules(final Set<AccountRule> rules) {
            accountRules = rules;
            return this;
        }

        public Builder addTransactionRules(final Set<TransactionRule> rules) {
            transactionRules = rules;
            return this;
        }

        public Builder validate(final AisData aisData) {
            for (final AisDataRule rule : aisDataRules) {
                final boolean passed = rule.getCriterion().test(aisData);
                final String msg = passed ? "" : rule.getMessage(aisData);
                subResults.put(rule.getRuleIdentifier(), new ValidationSubResult(passed, msg));
            }
            for (final AccountRule rule : accountRules) {
                boolean passed = true;
                String msg = "";
                for (final Account account : aisData.getAccounts()) {
                    if (!rule.getCriterion().test(account)) {
                        passed = false;
                        msg = rule.getMessage(account);
                        break;
                    }
                }
                subResults.put(rule.getRuleIdentifier(), new ValidationSubResult(passed, msg));
            }
            for (final TransactionRule rule : transactionRules) {
                boolean passed = true;
                String msg = "";
                for (final Transaction transaction : aisData.getTransactions()) {
                    if (!rule.getCriterion().test(transaction)) {
                        passed = false;
                        msg = rule.getMessage(transaction);
                        break;
                    }
                }
                subResults.put(rule.getRuleIdentifier(), new ValidationSubResult(passed, msg));
            }
            return this;
        }
    }
}
