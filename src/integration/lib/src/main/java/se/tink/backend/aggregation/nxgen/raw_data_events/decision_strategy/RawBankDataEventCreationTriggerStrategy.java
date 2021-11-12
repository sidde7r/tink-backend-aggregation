package se.tink.backend.aggregation.nxgen.raw_data_events.decision_strategy;

public interface RawBankDataEventCreationTriggerStrategy {
    boolean shouldTryProduceRawBankDataEvent();
}
