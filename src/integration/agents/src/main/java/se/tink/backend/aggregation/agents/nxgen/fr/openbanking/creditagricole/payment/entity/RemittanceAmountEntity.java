package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.creditagricole.payment.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class RemittanceAmountEntity {
    @JsonProperty("duePayableAmount")
    private AmountTypeEntity duePayableAmount = null;

    @JsonProperty("discountAppliedAmount")
    private TypedAmountEntity discountAppliedAmount = null;

    @JsonProperty("creditNoteAmount")
    private AmountTypeEntity creditNoteAmount = null;

    @JsonProperty("taxAmount")
    private TypedAmountEntity taxAmount = null;

    @JsonProperty("adjustmentAmountAndReason")
    private DocumentAdjustmentEntity adjustmentAmountAndReason = null;

    @JsonProperty("remittedAmount")
    private AmountTypeEntity remittedAmount = null;

    public AmountTypeEntity getDuePayableAmount() {
        return duePayableAmount;
    }

    public void setDuePayableAmount(AmountTypeEntity duePayableAmount) {
        this.duePayableAmount = duePayableAmount;
    }

    public TypedAmountEntity getDiscountAppliedAmount() {
        return discountAppliedAmount;
    }

    public void setDiscountAppliedAmount(TypedAmountEntity discountAppliedAmount) {
        this.discountAppliedAmount = discountAppliedAmount;
    }

    public AmountTypeEntity getCreditNoteAmount() {
        return creditNoteAmount;
    }

    public void setCreditNoteAmount(AmountTypeEntity creditNoteAmount) {
        this.creditNoteAmount = creditNoteAmount;
    }

    public TypedAmountEntity getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(TypedAmountEntity taxAmount) {
        this.taxAmount = taxAmount;
    }

    public DocumentAdjustmentEntity getAdjustmentAmountAndReason() {
        return adjustmentAmountAndReason;
    }

    public void setAdjustmentAmountAndReason(DocumentAdjustmentEntity adjustmentAmountAndReason) {
        this.adjustmentAmountAndReason = adjustmentAmountAndReason;
    }

    public AmountTypeEntity getRemittedAmount() {
        return remittedAmount;
    }

    public void setRemittedAmount(AmountTypeEntity remittedAmount) {
        this.remittedAmount = remittedAmount;
    }
}
