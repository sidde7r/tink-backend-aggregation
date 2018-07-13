package se.tink.backend.aggregation.agents.banks.seb.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class InsuranceEntity {
    @JsonProperty("FORS_NR")
    private String insuranceNumber;
    @JsonProperty("KOMPL_TARIFF_KOD")
    private String tariffCode;
    @JsonProperty("TYP")
    private String type;
    @JsonProperty("VERKS_GREN_KOD")
    private String accountType; // I'm really not sure what to call this, but the field value is "IPS" for one object.
    @JsonProperty("DETAIL_URL")
    private String detailUrl;

    public String getInsuranceNumber() {
        return insuranceNumber;
    }

    public void setInsuranceNumber(String insuranceNumber) {
        this.insuranceNumber = insuranceNumber;
    }

    public String getTariffCode() {
        return tariffCode;
    }

    public void setTariffCode(String tariffCode) {
        this.tariffCode = tariffCode;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public String getDetailUrl() {
        return detailUrl;
    }

    public void setDetailUrl(String detailUrl) {
        this.detailUrl = detailUrl;
    }
}
