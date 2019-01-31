package se.tink.backend.aggregation.agents.banks.lansforsakringar.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Optional;
import se.tink.backend.aggregation.agents.models.Instrument;
import se.tink.libraries.strings.StringUtils;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ShareEntity {
    private String name;
    private String isinCode;
    private String marketValue;
    private String numberOfUnits;
    private String acquisitionCostPerSecurity;
    private String acquisitionCost;
    private String growthInPercent;
    private Boolean watched;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIsinCode() {
        return isinCode;
    }

    public void setIsinCode(String isinCode) {
        this.isinCode = isinCode;
    }

    public String getMarketValue() {
        return marketValue;
    }

    public void setMarketValue(String marketValue) {
        this.marketValue = marketValue;
    }

    public String getNumberOfUnits() {
        return numberOfUnits;
    }

    public void setNumberOfUnits(String numberOfUnits) {
        this.numberOfUnits = numberOfUnits;
    }

    public String getAcquisitionCostPerSecurity() {
        return acquisitionCostPerSecurity;
    }

    public void setAcquisitionCostPerSecurity(String acquisitionCostPerSecurity) {
        this.acquisitionCostPerSecurity = acquisitionCostPerSecurity;
    }

    public String getAcquisitionCost() {
        return acquisitionCost;
    }

    public void setAcquisitionCost(String acquisitionCost) {
        this.acquisitionCost = acquisitionCost;
    }

    public String getGrowthInPercent() {
        return growthInPercent;
    }

    public void setGrowthInPercent(String growthInPercent) {
        this.growthInPercent = growthInPercent;
    }

    public Boolean getWatched() {
        return watched;
    }

    public void setWatched(Boolean watched) {
        this.watched = watched;
    }

    public Optional<Instrument> toInstrument(InstrumentDetailsEntity instrumentDetails) {
        Double quantity = parseStringToDouble(getNumberOfUnits());

        if (quantity == null) {
            return Optional.empty();
        }

        Instrument instrument = new Instrument();

        instrument.setAverageAcquisitionPrice(parseStringToDouble(getAcquisitionCostPerSecurity()));
        instrument.setCurrency(instrumentDetails != null ? instrumentDetails.getCurrency() : null);
        instrument.setIsin(getIsinCode());
        Double marketValue = parseStringToDouble(getMarketValue());
        instrument.setMarketValue(marketValue);
        instrument.setName(getName());
        instrument.setPrice(parseStringToDouble(instrumentDetails != null ? instrumentDetails.getBuyingPrice() : null));
        Double acquisitionCost = parseStringToDouble(getAcquisitionCost());
        instrument.setProfit(marketValue != null && acquisitionCost != null ? marketValue - acquisitionCost : null);
        instrument.setQuantity(quantity);
        instrument.setTicker(instrumentDetails != null ? instrumentDetails.getSymbol() : null);
        instrument.setType(Instrument.Type.STOCK);
        instrument.setUniqueIdentifier(instrumentDetails != null ?
                getIsinCode() + instrumentDetails.getSymbol().trim() : getIsinCode());

        return Optional.of(instrument);
    }

    private Double parseStringToDouble(String amount) {
        if (amount == null) {
            return null;
        }

        return StringUtils.parseAmount(amount);
    }
}
