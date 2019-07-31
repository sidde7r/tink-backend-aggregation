package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.creditagricole.payment.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class StructuredRemittanceInformationEntity {
    @JsonProperty("referredDocumentInformation")
    private ReferredDocumentInformationsEntity referredDocumentInformation = null;

    @JsonProperty("referredDocumentAmount")
    private RemittanceAmountEntity referredDocumentAmount = null;

    @JsonProperty("creditorReferenceInformation")
    private CreditorReferenceInformationEntity creditorReferenceInformation = null;

    @JsonProperty("invoicer")
    private PartyIdentificationEntity invoicer = null;

    @JsonProperty("invoicee")
    private PartyIdentificationEntity invoicee = null;

    @JsonProperty("taxRemittance")
    private TaxInformationEntity taxRemittance = null;

    @JsonProperty("garnishmentRemittance")
    private GarnishmentEntity garnishmentRemittance = null;

    public ReferredDocumentInformationsEntity getReferredDocumentInformation() {
        return referredDocumentInformation;
    }

    public void setReferredDocumentInformation(
            ReferredDocumentInformationsEntity referredDocumentInformation) {
        this.referredDocumentInformation = referredDocumentInformation;
    }

    public RemittanceAmountEntity getReferredDocumentAmount() {
        return referredDocumentAmount;
    }

    public void setReferredDocumentAmount(RemittanceAmountEntity referredDocumentAmount) {
        this.referredDocumentAmount = referredDocumentAmount;
    }

    public CreditorReferenceInformationEntity getCreditorReferenceInformation() {
        return creditorReferenceInformation;
    }

    public void setCreditorReferenceInformation(
            CreditorReferenceInformationEntity creditorReferenceInformation) {
        this.creditorReferenceInformation = creditorReferenceInformation;
    }

    public PartyIdentificationEntity getInvoicer() {
        return invoicer;
    }

    public void setInvoicer(PartyIdentificationEntity invoicer) {
        this.invoicer = invoicer;
    }

    public PartyIdentificationEntity getInvoicee() {
        return invoicee;
    }

    public void setInvoicee(PartyIdentificationEntity invoicee) {
        this.invoicee = invoicee;
    }

    public TaxInformationEntity getTaxRemittance() {
        return taxRemittance;
    }

    public void setTaxRemittance(TaxInformationEntity taxRemittance) {
        this.taxRemittance = taxRemittance;
    }

    public GarnishmentEntity getGarnishmentRemittance() {
        return garnishmentRemittance;
    }

    public void setGarnishmentRemittance(GarnishmentEntity garnishmentRemittance) {
        this.garnishmentRemittance = garnishmentRemittance;
    }
}
