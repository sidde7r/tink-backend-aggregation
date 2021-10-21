package se.tink.backend.aggregation.nxgen.http.event.decision_strategy;

import java.security.SecureRandom;

public class RandomStickyDecisionMakerRawBankDataEventCreationTriggerStrategy
        implements RawBankDataEventCreationTriggerStrategy {

    private final boolean stickyDecision;

    public RandomStickyDecisionMakerRawBankDataEventCreationTriggerStrategy(double rate) {
        stickyDecision = new SecureRandom().nextDouble() <= rate;
    }

    @Override
    public boolean shouldTryProduceRawBankDataEvent() {
        return stickyDecision;
    }
}
