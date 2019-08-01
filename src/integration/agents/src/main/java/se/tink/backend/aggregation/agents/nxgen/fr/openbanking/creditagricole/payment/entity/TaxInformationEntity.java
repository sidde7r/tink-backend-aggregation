package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.creditagricole.payment.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TaxInformationEntity {
    @JsonProperty("creditor")
    private TaxPartyEntity creditor = null;

    @JsonProperty("debtor")
    private TaxPartyEntity debtor = null;

    @JsonProperty("ultimateDebtor")
    private TaxPartyEntity ultimateDebtor = null;

    @JsonProperty("administrationZone")
    private String administrationZone = null;

    @JsonProperty("referenceNumber")
    private String referenceNumber = null;

    @JsonProperty("method")
    private String method = null;

    @JsonProperty("totalTaxableBaseAmount")
    private AmountTypeEntity totalTaxableBaseAmount = null;

    @JsonProperty("totalTaxAmount")
    private AmountTypeEntity totalTaxAmount = null;

    @JsonProperty("date")
    private LocalDate date = null;

    @JsonProperty("sequenceNumber")
    private BigDecimal sequenceNumber = null;

    @JsonProperty("record")
    private List<TaxRecordEntity> record = null;

    public TaxPartyEntity getCreditor() {
        return creditor;
    }

    public void setCreditor(TaxPartyEntity creditor) {
        this.creditor = creditor;
    }

    public TaxPartyEntity getDebtor() {
        return debtor;
    }

    public void setDebtor(TaxPartyEntity debtor) {
        this.debtor = debtor;
    }

    public TaxPartyEntity getUltimateDebtor() {
        return ultimateDebtor;
    }

    public void setUltimateDebtor(TaxPartyEntity ultimateDebtor) {
        this.ultimateDebtor = ultimateDebtor;
    }

    public String getAdministrationZone() {
        return administrationZone;
    }

    public void setAdministrationZone(String administrationZone) {
        this.administrationZone = administrationZone;
    }

    public String getReferenceNumber() {
        return referenceNumber;
    }

    public void setReferenceNumber(String referenceNumber) {
        this.referenceNumber = referenceNumber;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public AmountTypeEntity getTotalTaxableBaseAmount() {
        return totalTaxableBaseAmount;
    }

    public void setTotalTaxableBaseAmount(AmountTypeEntity totalTaxableBaseAmount) {
        this.totalTaxableBaseAmount = totalTaxableBaseAmount;
    }

    public AmountTypeEntity getTotalTaxAmount() {
        return totalTaxAmount;
    }

    public void setTotalTaxAmount(AmountTypeEntity totalTaxAmount) {
        this.totalTaxAmount = totalTaxAmount;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public BigDecimal getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(BigDecimal sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public List<TaxRecordEntity> getRecord() {
        return record;
    }

    public void setRecord(List<TaxRecordEntity> record) {
        this.record = record;
    }
}
