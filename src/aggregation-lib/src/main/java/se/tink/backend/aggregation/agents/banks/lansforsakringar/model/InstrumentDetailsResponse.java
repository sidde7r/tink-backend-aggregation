package se.tink.backend.aggregation.agents.banks.lansforsakringar.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class InstrumentDetailsResponse {
    private InstrumentDetailsEntity instrument;

    public InstrumentDetailsEntity getInstrument() {
        return instrument;
    }

    public void setInstrument(InstrumentDetailsEntity instrument) {
        this.instrument = instrument;
    }
}
