package se.tink.backend.aggregation.agents.banks.swedbank.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Objects;
import se.tink.backend.core.enums.TransferType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EInvoicePayment {

    private String amount;
    private String dueDate;
    private String einvoiceReferens;
    private PaymentAccountEntity payee;
    private String paymentType;
    private PaymentReference reference;
    private LinksEntity links;

    public String getAmount() {
        return amount;
    }

    public String getDueDate() {
        return dueDate;
    }

    public String getEinvoiceReferens() {
        return einvoiceReferens;
    }

    public PaymentAccountEntity getPayee() {
        return payee;
    }

    public String getPaymentType() {
        return paymentType;
    }

    public PaymentReference getReference() {
        return reference;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public void setEinvoiceReferens(String einvoiceReferens) {
        this.einvoiceReferens = einvoiceReferens;
    }

    public void setPayee(PaymentAccountEntity payee) {
        this.payee = payee;
    }

    public void setPaymentType(String paymentType) {
        this.paymentType = paymentType;
    }

    public void setReference(PaymentReference reference) {
        this.reference = reference;
    }

    @JsonIgnore
    public TransferType getTransferType() {
        if (Objects.equal(this.paymentType, "DOMESTIC")) {
            return TransferType.PAYMENT;
        } else if (Objects.equal(this.paymentType, "EINVOICE")) {
            return TransferType.EINVOICE;
        }
        return null;
    }

    public LinksEntity getLinks() {
        return links;
    }

    public void setLinks(LinksEntity links) {
        this.links = links;
    }
}
