package se.tink.backend.aggregation.agents.abnamro.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomerEntity {
    private String bcNumber;
    private String interpayName;

    public String getBcNumber() {
        return bcNumber;
    }

    public void setBcNumber(String bcNumber) {
        this.bcNumber = bcNumber;
    }

    public String getInterpayName() {
        return interpayName;
    }

    public void setInterpayName(String interpayName) {
        this.interpayName = interpayName;
    }
}
