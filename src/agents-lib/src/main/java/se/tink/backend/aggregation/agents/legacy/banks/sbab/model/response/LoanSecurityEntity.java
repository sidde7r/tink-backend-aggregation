package se.tink.backend.aggregation.agents.banks.sbab.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LoanSecurityEntity {

    @JsonProperty("beteckning")
    private String label;

    @JsonProperty("kommunnamn")
    private String municipalityName;

    @JsonProperty("manadsavgift")
    private int monthlyFee;

    @JsonProperty("objekt")
    private SecurityEntity security;

    @JsonProperty("objektId")
    private String securityId;

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getMunicipalityName() {
        return municipalityName;
    }

    public void setMunicipalityName(String municipalityName) {
        this.municipalityName = municipalityName;
    }

    public int getMonthlyFee() {
        return monthlyFee;
    }

    public void setMonthlyFee(int monthlyFee) {
        this.monthlyFee = monthlyFee;
    }

    public SecurityEntity getSecurity() {
        return security;
    }

    public void setSecurity(SecurityEntity security) {
        this.security = security;
    }

    public String getSecurityId() {
        return securityId;
    }

    public void setSecurityId(String securityId) {
        this.securityId = securityId;
    }
}
