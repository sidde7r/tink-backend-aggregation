package se.tink.backend.aggregation.agents.banks.se.collector.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GeneratePdfAgreementRequest {
    @JsonProperty("namn")
    private String name;
    @JsonProperty("prnr")
    private String ssn;

    public String getSsn() {
        return ssn;
    }

    public void setSsn(String ssn) {
        this.ssn = ssn;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
