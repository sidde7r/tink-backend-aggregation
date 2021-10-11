package se.tink.backend.aggregation.nxgen.http.event.decision_strategy;

public class AllowAlwaysRawBankDataEventEmissionDecisionStrategy
        implements RawBankDataEventEmissionDecisionStrategy {

    @Override
    public boolean shouldEmitRawBankDataEvent() {
        return true;
    }
}
