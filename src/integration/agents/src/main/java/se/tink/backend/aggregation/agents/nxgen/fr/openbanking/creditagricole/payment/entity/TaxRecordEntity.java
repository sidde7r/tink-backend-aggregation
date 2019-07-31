package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.creditagricole.payment.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TaxRecordEntity {
    @JsonProperty("type")
    private String type = null;

    @JsonProperty("category")
    private String category = null;

    @JsonProperty("categoryDetails")
    private String categoryDetails = null;

    @JsonProperty("debtorStatus")
    private String debtorStatus = null;

    @JsonProperty("certificateIdentification")
    private String certificateIdentification = null;

    @JsonProperty("formsCode")
    private String formsCode = null;

    @JsonProperty("period")
    private TaxPeriodEntity period = null;

    @JsonProperty("taxAmount")
    private TaxAmountEntity taxAmount = null;

    @JsonProperty("additionalInformation")
    private String additionalInformation = null;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getCategoryDetails() {
        return categoryDetails;
    }

    public void setCategoryDetails(String categoryDetails) {
        this.categoryDetails = categoryDetails;
    }

    public String getDebtorStatus() {
        return debtorStatus;
    }

    public void setDebtorStatus(String debtorStatus) {
        this.debtorStatus = debtorStatus;
    }

    public String getCertificateIdentification() {
        return certificateIdentification;
    }

    public void setCertificateIdentification(String certificateIdentification) {
        this.certificateIdentification = certificateIdentification;
    }

    public String getFormsCode() {
        return formsCode;
    }

    public void setFormsCode(String formsCode) {
        this.formsCode = formsCode;
    }

    public TaxPeriodEntity getPeriod() {
        return period;
    }

    public void setPeriod(TaxPeriodEntity period) {
        this.period = period;
    }

    public TaxAmountEntity getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(TaxAmountEntity taxAmount) {
        this.taxAmount = taxAmount;
    }

    public String getAdditionalInformation() {
        return additionalInformation;
    }

    public void setAdditionalInformation(String additionalInformation) {
        this.additionalInformation = additionalInformation;
    }
}
