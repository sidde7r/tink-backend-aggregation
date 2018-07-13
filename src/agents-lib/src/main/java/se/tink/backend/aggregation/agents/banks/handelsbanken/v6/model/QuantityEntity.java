package se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class QuantityEntity {
    private String quantityFormatted;
    private String unit;

    public String getQuantityFormatted() {
        return quantityFormatted;
    }

    public void setQuantityFormatted(String quantityFormatted) {
        this.quantityFormatted = quantityFormatted;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }
}
