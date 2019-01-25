package se.tink.backend.aggregation.agents.banks.nordea.v20.model.investments;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.system.rpc.Instrument;
import se.tink.libraries.strings.StringUtils;

@JsonIgnoreProperties(ignoreUnknown = true)
public class InstrumentEntity {
    private InstrumentIdEntity instrumentId;
    private String instrumentName;
    private String instrumentType;
    private String price;
    private String priceTime;
    private String todaysChange;
    @JsonProperty("todaysChangePct")
    private String todaysChangePercentage;

    public InstrumentIdEntity getInstrumentId() {
        return instrumentId;
    }

    public void setInstrumentId(
            InstrumentIdEntity instrumentId) {
        this.instrumentId = instrumentId;
    }

    public String getInstrumentName() {
        return instrumentName;
    }

    public void setInstrumentName(String instrumentName) {
        this.instrumentName = instrumentName;
    }

    public String getInstrumentType() {
        return instrumentType;
    }

    public void setInstrumentType(String instrumentType) {
        this.instrumentType = instrumentType;
    }

    public Double getPrice() {
        return price == null || price.isEmpty() ? null : StringUtils.parseAmount(price);
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getPriceTime() {
        return priceTime;
    }

    public void setPriceTime(String priceTime) {
        this.priceTime = priceTime;
    }

    public String getTodaysChange() {
        return todaysChange;
    }

    public void setTodaysChange(String todaysChange) {
        this.todaysChange = todaysChange;
    }

    public String getTodaysChangePercentage() {
        return todaysChangePercentage;
    }

    public void setTodaysChangePercentage(String todaysChangePercentage) {
        this.todaysChangePercentage = todaysChangePercentage;
    }

    Instrument.Type getTinkInstrumentType() {
        switch (getInstrumentType().toLowerCase()) {
        case "equity":
            return Instrument.Type.STOCK;
        case "fund":
            return Instrument.Type.FUND;
        case "derivative":
            // Intentional fall through
        default:
            return Instrument.Type.OTHER;
        }
    }
}
