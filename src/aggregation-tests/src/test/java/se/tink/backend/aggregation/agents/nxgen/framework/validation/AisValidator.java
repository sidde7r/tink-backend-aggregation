package se.tink.backend.aggregation.agents.nxgen.framework.validation;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

public final class AisValidator {
    private Set<ValidationRule> rules;
    private final Action action = new WarnAction();

    private AisValidator() {}

    public static Builder builder() {
        return new Builder();
    }

    public void validate(final AisData aisData) {
        for (final ValidationRule rule : rules) {
            if (rule.getAisDataCriterion().test(aisData)) {
                action.onPass(aisData, rule.getRuleIdentifier());
            } else {
                action.onFail(aisData, rule.getRuleIdentifier(), rule.getAisDataMessage(aisData));
            }
        }
    }

    public static final class Builder {
        private Set<ValidationRule> rules = new HashSet<>();

        private Builder() {}

        public Builder rule(
                final String ruleName,
                final Predicate<AisData> criterion,
                final Function<AisData, String> failMessage) {
            rules.add(new ValidationRule(ruleName, criterion, failMessage));
            return this;
        }

        public AisValidator build() {
            final AisValidator validator = new AisValidator();
            validator.rules = rules;
            return validator;
        }
    }
}
