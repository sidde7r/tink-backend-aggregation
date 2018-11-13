package se.tink.backend.aggregation.agents.nxgen.framework.validation;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import se.tink.backend.aggregation.rpc.Account;

/**
 * A rule is uniquely identified and recognized by its string identifier.
 */
public final class ValidationRule {
    private String ruleIdentifier;

    private Predicate<AisData> aisDataCriterion;
    private Function<AisData, String> aisDataMessage;

    private Predicate<Account> accountCriterion;
    private Function<Account, String> accountMessage;

    private ValidationRule() {}

    public static ValidationRule fromAisData(
            final String ruleIdentifier,
            final Predicate<AisData> criterion,
            final Function<AisData, String> failMessage) {
        final ValidationRule rule = new ValidationRule();
        rule.ruleIdentifier = ruleIdentifier;

        rule.aisDataCriterion = criterion;
        rule.aisDataMessage = failMessage;

        rule.accountCriterion = null;
        rule.accountMessage = null;

        return rule;
    }

    public static ValidationRule fromAccount(
            final String ruleIdentifier,
            final Predicate<Account> criterion,
            final Function<Account, String> failMessage) {
        final ValidationRule rule = new ValidationRule();

        rule.ruleIdentifier = ruleIdentifier;

        rule.aisDataCriterion = null;
        rule.aisDataMessage = null;

        rule.accountCriterion = criterion;
        rule.accountMessage = failMessage;
        return rule;
    }

    public String getRuleIdentifier() {
        return ruleIdentifier;
    }

    public Predicate<AisData> getAisDataCriterion() {
        return aisDataCriterion;
    }

    public String getMessage(final AisData aisData) {
        return aisDataMessage.apply(aisData);
    }

    public Predicate<Account> getAccountCriterion() {
        return accountCriterion;
    }

    public String getMessage(final Account account) {
        return accountMessage.apply(account);
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        } else if (o == null) {
            return false;
        } else if (!(o instanceof ValidationRule)) {
            return false;
        }
        final ValidationRule other = (ValidationRule) o;
        return Objects.equals(ruleIdentifier, other.getRuleIdentifier());
    }

    @Override
    public int hashCode() {
        return ruleIdentifier.hashCode();
    }
}
