package se.tink.backend.aggregation.nxgen.raw_data_events.decision_strategy;

public class DenyAlwaysRawBankDataEventCreationTriggerStrategy
        implements RawBankDataEventCreationTriggerStrategy {

    @Override
    public boolean shouldTryProduceRawBankDataEvent() {
        return false;
    }
}
