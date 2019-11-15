package se.tink.backend.aggregation.nxgen.framework.validation;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import se.tink.backend.agents.rpc.Account;

public final class AccountRule implements ValidationRule<Account> {
    private final String ruleIdentifier;
    private final Predicate<Account> criterion;
    private final Function<Account, String> failMessage;

    public AccountRule(
            final String ruleIdentifier,
            final Predicate<Account> criterion,
            final Function<Account, String> failMessage) {
        this.ruleIdentifier = ruleIdentifier;
        this.criterion = criterion;
        this.failMessage = failMessage;
    }

    @Override
    public String getRuleIdentifier() {
        return ruleIdentifier;
    }

    @Override
    public Predicate<Account> getCriterion() {
        return criterion;
    }

    @Override
    public String getMessage(final Account account) {
        return failMessage.apply(account);
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        } else if (o == null) {
            return false;
        } else if (!(o instanceof AccountRule)) {
            return false;
        }
        final AccountRule other = (AccountRule) o;
        return Objects.equals(ruleIdentifier, other.getRuleIdentifier());
    }

    @Override
    public int hashCode() {
        return ruleIdentifier.hashCode();
    }
}
