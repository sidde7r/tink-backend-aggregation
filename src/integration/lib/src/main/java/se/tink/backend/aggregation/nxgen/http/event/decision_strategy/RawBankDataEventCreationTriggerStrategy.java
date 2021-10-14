package se.tink.backend.aggregation.nxgen.http.event.decision_strategy;

public interface RawBankDataEventCreationTriggerStrategy {
    boolean shouldTryProduceRawBankDataEvent();
}
