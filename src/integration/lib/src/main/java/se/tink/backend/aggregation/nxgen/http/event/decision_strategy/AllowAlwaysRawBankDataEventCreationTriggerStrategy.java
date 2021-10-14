package se.tink.backend.aggregation.nxgen.http.event.decision_strategy;

public class AllowAlwaysRawBankDataEventCreationTriggerStrategy
        implements RawBankDataEventCreationTriggerStrategy {

    @Override
    public boolean shouldTryProduceRawBankDataEvent() {
        return true;
    }
}
