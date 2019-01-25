package se.tink.backend.aggregation.agents.banks.lansforsakringar.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Optional;
import se.tink.backend.aggregation.agents.models.Instrument;
import se.tink.libraries.strings.StringUtils;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class BondEntity {
    @JsonIgnore
    private static final String CURRENCY = "SEK"; // Not available from LF
    private String name;
    private String isinCode;
    private String marketValue;
    private String numberOfUnits;
    private String acquisitionCostPerSecurity;
    private String acquisitionCost;
    private String growthInPercent;
    private boolean watched;

    public String getName() {
        return name;
    }

    public String getIsinCode() {
        return isinCode;
    }

    public double getMarketValue() {
        return marketValue != null ? StringUtils.parseAmount(marketValue) : 0;
    }

    public Double getNumberOfUnits() {
        return numberOfUnits != null ? StringUtils.parseAmount(numberOfUnits) : null;
    }

    public Double getAcquisitionCostPerSecurity() {
        return acquisitionCostPerSecurity != null ? StringUtils.parseAmount(acquisitionCostPerSecurity) : null;
    }

    public Double getAcquisitionCost() {
        return acquisitionCost != null ? StringUtils.parseAmount(acquisitionCost) : 0;
    }

    public String getGrowthInPercent() {
        return growthInPercent;
    }

    public boolean isWatched() {
        return watched;
    }

    public Optional<Instrument> toInstrument() {
        Double quantity = getNumberOfUnits();
        if (quantity == null || quantity == 0) {
            return Optional.empty();
        }

        Instrument instrument = new Instrument();

        instrument.setAverageAcquisitionPrice(getAcquisitionCostPerSecurity());
        instrument.setCurrency(CURRENCY);
        instrument.setIsin(getIsinCode());
        double marketValue = getMarketValue();
        instrument.setMarketValue(marketValue);
        instrument.setName(getName());
        instrument.setPrice(marketValue / quantity);
        instrument.setProfit(getMarketValue() - getAcquisitionCost());
        instrument.setQuantity(quantity);
        instrument.setType(Instrument.Type.OTHER);
        instrument.setUniqueIdentifier(getIsinCode()+getName().replaceAll(" ", ""));

        return Optional.of(instrument);
    }
}
