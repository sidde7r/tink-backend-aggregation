package se.tink.backend.aggregation.agents.banks.sbab.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LoanApplicantPartEntity {

    @JsonProperty("orgnr")
    private String identityNumber;

    @JsonProperty("partId")
    private int partId;

    @JsonProperty("ranteAndelRSV")
    private double partOfLoan;

    public String getIdentityNumber() {
        return identityNumber;
    }

    public void setIdentityNumber(String identityNumber) {
        this.identityNumber = identityNumber;
    }

    public int getPartId() {
        return partId;
    }

    public void setPartId(int partId) {
        this.partId = partId;
    }

    public double getPartOfLoan() {
        return partOfLoan;
    }

    public void setPartOfLoan(double partOfLoan) {
        this.partOfLoan = partOfLoan;
    }
}
