package se.tink.backend.aggregation.agents.banks.swedbank.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import se.tink.backend.core.enums.TransferType;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TransferTransactionDetails {
    private TransactionAccountEntity toAccount;
    private String dateDependency;
    private String noteToRecipient;
    private String type;
    private String status;
    private PaymentReference reference;
    private PaymentAccountEntity payee;
    private String einvoiceReference;
    private String rejectionMessage;
    private String amount;
    private String date;
    private String dueDate;


    public TransactionAccountEntity getToAccount() {
        return toAccount;
    }

    public void setToAccount(TransactionAccountEntity toAccount) {
        this.toAccount = toAccount;
    }

    public String getDateDependency() {
        return Strings.isNullOrEmpty(dateDependency) ? null : dateDependency.toUpperCase();
    }

    public void setDateDependency(String dateDependency) {
        this.dateDependency = dateDependency;
    }

    public String getNoteToRecipient() {
        return noteToRecipient;
    }

    public void setNoteToRecipient(String noteToRecipient) {
        this.noteToRecipient = noteToRecipient;
    }

    public String getType() {
        return Strings.isNullOrEmpty(type) ? null : type.toUpperCase();
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStatus() {
        return Strings.isNullOrEmpty(status) ? null : status.toUpperCase();
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public PaymentReference getReference() {
        return reference;
    }

    public void setReference(PaymentReference reference) {
        this.reference = reference;
    }

    public PaymentAccountEntity getPayee() {
        return payee;
    }

    public void setPayee(PaymentAccountEntity payee) {
        this.payee = payee;
    }

    public String getEinvoiceReference() {
        return einvoiceReference;
    }

    public void setEnivoiceReference(String einvoiceReference) {
        this.einvoiceReference = einvoiceReference;
    }

    public String getRejectionMessage() {
        return rejectionMessage;
    }

    public void setRejectionMessage(String rejectionMessage) {
        this.rejectionMessage = rejectionMessage;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    @JsonIgnore
    public TransferType getTransferType() {
        if (Objects.equal(getType(), "DOMESTIC")) {
            return TransferType.PAYMENT;
        } else if (Objects.equal(getType(), "EINVOICE")) {
            return TransferType.EINVOICE;
        }
        return null;
    }
}
