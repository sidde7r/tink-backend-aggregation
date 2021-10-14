package se.tink.backend.aggregation.nxgen.http.event.decision_strategy;

public class DenyAlwaysRawBankDataEventCreationTriggerStrategy
        implements RawBankDataEventCreationTriggerStrategy {

    @Override
    public boolean shouldTryProduceRawBankDataEvent() {
        return false;
    }
}
