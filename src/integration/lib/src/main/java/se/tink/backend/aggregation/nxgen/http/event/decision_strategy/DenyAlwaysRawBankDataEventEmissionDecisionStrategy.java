package se.tink.backend.aggregation.nxgen.http.event.decision_strategy;

public class DenyAlwaysRawBankDataEventEmissionDecisionStrategy
        implements RawBankDataEventEmissionDecisionStrategy {

    @Override
    public boolean shouldEmitRawBankDataEvent() {
        return false;
    }
}
