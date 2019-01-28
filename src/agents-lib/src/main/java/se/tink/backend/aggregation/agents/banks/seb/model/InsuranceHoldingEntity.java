package se.tink.backend.aggregation.agents.banks.seb.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.agents.models.Instrument;

@JsonObject
public class InsuranceHoldingEntity {
    @JsonIgnore
    private static final Logger log = LoggerFactory.getLogger(InsuranceHoldingEntity.class);

    @JsonProperty("FORS_NR")
    private String insuranceNumber;
    @JsonProperty("FOND_NR")
    private String fundNumber;
    @JsonProperty("FOND_NAMN_SMA")
    private String name;
    @JsonProperty("FOND_KURS_BELOPP")
    private Double marketValue;
    @JsonProperty("ANDEL_ANTAL")
    private Double quantity;
    @JsonProperty("ANDEL_BELOPP")
    private Double totalMarketValue;
    @JsonProperty("RORL_KOSTN_FORS")
    private Double variableCost;
    @JsonProperty("FAST_KOSTN_FORS")
    private Double fixedCost;
    @JsonProperty("GAV")
    private Double averageAcquisitionCost;
    @JsonProperty("ANSKAFF_KOSTNAD")
    private Double acquisitionCost;

    @JsonIgnore
    public Optional<Instrument> toInstrument() {
        if (quantity == 0) {
            return Optional.empty();
        }

        Instrument instrument = new Instrument();

        instrument.setUniqueIdentifier(getUniqueIdentifier());
        instrument.setQuantity(quantity);
        instrument.setName(name);
        instrument.setType(Instrument.Type.FUND);
        instrument.setRawType(name);
        instrument.setAverageAcquisitionPrice(averageAcquisitionCost);
        instrument.setMarketValue(totalMarketValue);
        instrument.setPrice(marketValue);
        instrument.setProfit(totalMarketValue - acquisitionCost);

        return Optional.of(instrument);
    }

    @JsonIgnore
    private String getUniqueIdentifier() {
        if (Strings.isNullOrEmpty(fundNumber)) {
            log.warn("Fund number not present for instrument. Fund name set as unique identifier.");
            return name.replaceAll(" ", "");
        }

        return fundNumber + "-SEB-SE";
    }

    public String getInsuranceNumber() {
        return insuranceNumber;
    }

    public String getFundNumber() {
        return fundNumber;
    }

    public String getName() {
        return name;
    }

    public Double getMarketValue() {
        return marketValue;
    }

    public Double getQuantity() {
        return quantity;
    }

    public Double getTotalMarketValue() {
        return totalMarketValue;
    }

    public Double getVariableCost() {
        return variableCost;
    }

    public Double getFixedCost() {
        return fixedCost;
    }

    public Double getAverageAcquisitionCost() {
        return averageAcquisitionCost;
    }

    public Double getAcquisitionCost() {
        return acquisitionCost;
    }
}
