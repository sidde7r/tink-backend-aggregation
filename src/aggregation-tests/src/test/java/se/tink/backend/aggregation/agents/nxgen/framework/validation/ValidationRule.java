package se.tink.backend.aggregation.agents.nxgen.framework.validation;

import java.util.function.Function;
import java.util.function.Predicate;

public final class ValidationRule {
    private final String ruleIdentifier;

    private final Predicate<AisData> aisDataCriterion;
    private final Function<AisData, String> aisDataMessage;

    public ValidationRule(final String ruleIdentifier, final Predicate<AisData> condition, final Function<AisData, String> failMessage) {
        this.ruleIdentifier = ruleIdentifier;
        this.aisDataCriterion = condition;
        this.aisDataMessage = failMessage;
    }

    public String getRuleIdentifier() {
        return ruleIdentifier;
    }

    public Predicate<AisData> getAisDataCriterion() {
        return aisDataCriterion;
    }

    public String getAisDataMessage(final AisData aisData) {
        return aisDataMessage.apply(aisData);
    }
}
