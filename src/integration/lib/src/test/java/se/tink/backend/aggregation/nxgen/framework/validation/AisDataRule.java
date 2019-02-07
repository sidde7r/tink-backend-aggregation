package se.tink.backend.aggregation.nxgen.framework.validation;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * A rule is uniquely identified and recognized by its string identifier.
 */
public final class AisDataRule implements ValidationRule<AisData> {
    private String ruleIdentifier;

    private Predicate<AisData> criterion;
    private Function<AisData, String> failMessage;

    public AisDataRule(
            final String ruleIdentifier,
            final Predicate<AisData> criterion,
            final Function<AisData, String> failMessage) {
        this.ruleIdentifier = ruleIdentifier;
        this.criterion = criterion;
        this.failMessage = failMessage;
    }

    @Override
    public String getRuleIdentifier() {
        return ruleIdentifier;
    }

    @Override
    public Predicate<AisData> getCriterion() {
        return criterion;
    }

    @Override
    public String getMessage(final AisData aisData) {
        return failMessage.apply(aisData);
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        } else if (o == null) {
            return false;
        } else if (!(o instanceof AisDataRule)) {
            return false;
        }
        final AisDataRule other = (AisDataRule) o;
        return Objects.equals(ruleIdentifier, other.getRuleIdentifier());
    }

    @Override
    public int hashCode() {
        return ruleIdentifier.hashCode();
    }
}
