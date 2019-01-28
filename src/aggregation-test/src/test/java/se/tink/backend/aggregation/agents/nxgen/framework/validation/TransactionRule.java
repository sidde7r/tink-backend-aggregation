package se.tink.backend.aggregation.agents.nxgen.framework.validation;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import se.tink.backend.aggregation.agents.models.Transaction;

public final class TransactionRule implements ValidationRule<Transaction> {
    private final String ruleIdentifier;
    private final Predicate<Transaction> criterion;
    private final Function<Transaction, String> failMessage;

    public TransactionRule(
            final String ruleIdentifier,
            final Predicate<Transaction> criterion,
            final Function<Transaction, String> failMessage) {
        this.ruleIdentifier = ruleIdentifier;
        this.criterion = criterion;
        this.failMessage = failMessage;
    }

    @Override
    public String getRuleIdentifier() {
        return ruleIdentifier;
    }

    @Override
    public Predicate<Transaction> getCriterion() {
        return criterion;
    }

    @Override
    public String getMessage(final Transaction transaction) {
        return failMessage.apply(transaction);
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        } else if (o == null) {
            return false;
        } else if (!(o instanceof TransactionRule)) {
            return false;
        }
        final TransactionRule other = (TransactionRule) o;
        return Objects.equals(ruleIdentifier, other.getRuleIdentifier());
    }

    @Override
    public int hashCode() {
        return ruleIdentifier.hashCode();
    }
}
