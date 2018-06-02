package se.tink.backend.aggregation.agents.banks.swedbank.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class HoldingsEntity {
    private List<InstrumentsEntity> instruments;
    private String instrumentType;
    private String instrumentText;

    public List<InstrumentsEntity> getInstruments() {
        return instruments;
    }

    public void setInstruments(List<InstrumentsEntity> instruments) {
        this.instruments = instruments;
    }

    public String getInstrumentType() {
        return instrumentType;
    }

    public void setInstrumentType(String instrumentType) {
        this.instrumentType = instrumentType;
    }

    public String getInstrumentText() {
        return instrumentText;
    }

    public void setInstrumentText(String instrumentText) {
        this.instrumentText = instrumentText;
    }
}
