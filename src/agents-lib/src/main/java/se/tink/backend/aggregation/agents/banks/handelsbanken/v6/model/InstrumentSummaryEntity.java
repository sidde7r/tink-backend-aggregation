package se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class InstrumentSummaryEntity {
    private InstrumentLatestEntity instrumentLatest;
    private TradeSummaryEntity tradeSummary;
    private InstrumentLatestEntity underlyingInstrumentLatest;
    private InstrumentDetailWrapper instrumentDetail;

    public InstrumentLatestEntity getInstrumentLatest() {
        return instrumentLatest;
    }

    public void setInstrumentLatest(
            InstrumentLatestEntity instrumentLatest) {
        this.instrumentLatest = instrumentLatest;
    }

    public TradeSummaryEntity getTradeSummary() {
        return tradeSummary;
    }

    public void setTradeSummary(TradeSummaryEntity tradeSummary) {
        this.tradeSummary = tradeSummary;
    }

    public InstrumentLatestEntity getUnderlyingInstrumentLatest() {
        return underlyingInstrumentLatest;
    }

    public void setUnderlyingInstrumentLatest(
            InstrumentLatestEntity underlyingInstrumentLatest) {
        this.underlyingInstrumentLatest = underlyingInstrumentLatest;
    }

    public InstrumentDetailWrapper getInstrumentDetail() {
        return instrumentDetail;
    }

    public void setInstrumentDetail(
            InstrumentDetailWrapper instrumentDetail) {
        this.instrumentDetail = instrumentDetail;
    }
}
