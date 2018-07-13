package se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Property {
    private String label;
    private String value;

    public String getLabel() {
        return label != null ? label.toLowerCase() : null;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getValue() {
        return value != null ? value.toLowerCase() : null;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
