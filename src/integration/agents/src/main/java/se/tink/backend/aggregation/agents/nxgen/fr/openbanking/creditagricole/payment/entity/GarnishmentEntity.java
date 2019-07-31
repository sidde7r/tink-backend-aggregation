package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.creditagricole.payment.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class GarnishmentEntity {
    @JsonProperty("type")
    private CodeAndIssuerEntity type = null;

    @JsonProperty("garnishee")
    private TaxPartyEntity garnishee = null;

    @JsonProperty("garnishmentAdministrator")
    private TaxPartyEntity garnishmentAdministrator = null;

    @JsonProperty("referenceNumber")
    private String referenceNumber = null;

    @JsonProperty("date")
    private LocalDate date = null;

    @JsonProperty("remittedAmount")
    private AmountTypeEntity remittedAmount = null;

    @JsonProperty("familyMedicalInsuranceIndicator")
    private Boolean familyMedicalInsuranceIndicator = null;

    @JsonProperty("employeeTerminationIndicator")
    private Boolean employeeTerminationIndicator = null;

    public CodeAndIssuerEntity getType() {
        return type;
    }

    public void setType(CodeAndIssuerEntity type) {
        this.type = type;
    }

    public TaxPartyEntity getGarnishee() {
        return garnishee;
    }

    public void setGarnishee(TaxPartyEntity garnishee) {
        this.garnishee = garnishee;
    }

    public TaxPartyEntity getGarnishmentAdministrator() {
        return garnishmentAdministrator;
    }

    public void setGarnishmentAdministrator(TaxPartyEntity garnishmentAdministrator) {
        this.garnishmentAdministrator = garnishmentAdministrator;
    }

    public String getReferenceNumber() {
        return referenceNumber;
    }

    public void setReferenceNumber(String referenceNumber) {
        this.referenceNumber = referenceNumber;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public AmountTypeEntity getRemittedAmount() {
        return remittedAmount;
    }

    public void setRemittedAmount(AmountTypeEntity remittedAmount) {
        this.remittedAmount = remittedAmount;
    }

    public Boolean getFamilyMedicalInsuranceIndicator() {
        return familyMedicalInsuranceIndicator;
    }

    public void setFamilyMedicalInsuranceIndicator(Boolean familyMedicalInsuranceIndicator) {
        this.familyMedicalInsuranceIndicator = familyMedicalInsuranceIndicator;
    }

    public Boolean getEmployeeTerminationIndicator() {
        return employeeTerminationIndicator;
    }

    public void setEmployeeTerminationIndicator(Boolean employeeTerminationIndicator) {
        this.employeeTerminationIndicator = employeeTerminationIndicator;
    }
}
