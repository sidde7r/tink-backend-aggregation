package se.tink.backend.aggregation.nxgen.raw_data_events.decision_strategy;

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
