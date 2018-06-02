package se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TradeSummaryEntity extends AbstractResponse {
    private InstrumentLatestEntity instrumentLatest;

    public InstrumentLatestEntity getInstrumentLatest() {
        return instrumentLatest;
    }

    public void setInstrumentLatest(
            InstrumentLatestEntity instrumentLatest) {
        this.instrumentLatest = instrumentLatest;
    }
}
