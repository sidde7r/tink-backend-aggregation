package se.tink.backend.aggregation.agents.banks.seb.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class InsuranceAccountEntity {
    @JsonProperty("FORS_NR")
    private String insuranceNumber;
    @JsonProperty("FORSTYP")
    private String insuranceType;
    @JsonProperty("VARDE_BELOPP")
    private Double amount;
    @JsonProperty("VAERDE_DATUM")
    private Long amountDate;
    @JsonProperty("PREMIE_BELOPP")
    private Double premium;
    @JsonProperty("DETAIL_URL")
    private String detailUrl;

    public String getInsuranceNumber() {
        return insuranceNumber;
    }

    public void setInsuranceNumber(String insuranceNumber) {
        this.insuranceNumber = insuranceNumber;
    }

    public String getInsuranceType() {
        return insuranceType;
    }

    public void setInsuranceType(String insuranceType) {
        this.insuranceType = insuranceType;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public Long getAmountDate() {
        return amountDate;
    }

    public void setAmountDate(Long amountDate) {
        this.amountDate = amountDate;
    }

    public Double getPremium() {
        return premium;
    }

    public void setPremium(Double premium) {
        this.premium = premium;
    }

    public String getDetailUrl() {
        return detailUrl;
    }

    public void setDetailUrl(String detailUrl) {
        this.detailUrl = detailUrl;
    }
}
