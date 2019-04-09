package se.tink.backend.aggregation.agents.banks.nordea.v20.model.payments;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.Date;
import se.tink.backend.aggregation.agents.banks.nordea.NordeaHashMapSerializer;

public class ChangePaymentIn {
    @JsonSerialize(using = NordeaHashMapSerializer.String.class)
    private String statusCode;

    @JsonSerialize(using = NordeaHashMapSerializer.String.class)
    private String paymentSubType;

    @JsonSerialize(using = NordeaHashMapSerializer.String.class)
    private String messageRow;

    @JsonSerialize(using = NordeaHashMapSerializer.String.class)
    private String dueDateType;

    @JsonSerialize(using = NordeaHashMapSerializer.Boolean.class)
    private Boolean recurringContinuously;

    @JsonSerialize(using = NordeaHashMapSerializer.DailyDate.class)
    private Date dueDate;

    @JsonSerialize(using = NordeaHashMapSerializer.String.class)
    private String beneficiaryName;

    @JsonSerialize(using = NordeaHashMapSerializer.Double.class)
    private Double amount;

    @JsonSerialize(using = NordeaHashMapSerializer.Integer.class)
    private Integer recurringNumberOfPayments;

    @JsonSerialize(using = NordeaHashMapSerializer.String.class)
    private String paymentSubTypeExtension;

    @JsonSerialize(using = NordeaHashMapSerializer.String.class)
    private String recurringFrequency;

    @JsonSerialize(using = NordeaHashMapSerializer.String.class)
    private String toAccountId;

    @JsonSerialize(using = NordeaHashMapSerializer.String.class)
    private String fromAccountId;

    @JsonSerialize(using = NordeaHashMapSerializer.String.class)
    private String receiptCode;

    @JsonSerialize(using = NordeaHashMapSerializer.String.class)
    private String currency;

    @JsonSerialize(using = NordeaHashMapSerializer.Boolean.class)
    private Boolean addBeneficiary;

    @JsonSerialize(using = NordeaHashMapSerializer.YesNo.class)
    private Boolean storePayment;

    @JsonSerialize(using = NordeaHashMapSerializer.String.class)
    private String beneficiaryNickName;

    @JsonSerialize(using = NordeaHashMapSerializer.StatusCode.class)
    public Payment.StatusCode getStatusCode() {
        return Payment.StatusCode.fromSerializedValue(statusCode);
    }

    @JsonSerialize(using = NordeaHashMapSerializer.SubType.class)
    public Payment.SubType getPaymentSubType() {
        return Payment.SubType.fromSerializedValue(paymentSubType);
    }

    @JsonSerialize(using = NordeaHashMapSerializer.SubTypeExtension.class)
    public Payment.SubTypeExtension getPaymentSubTypeExtension() {
        return Payment.SubTypeExtension.fromSerializedValue(paymentSubTypeExtension);
    }

    public void setStatusCode(Payment.StatusCode statusCode) {
        this.statusCode = statusCode.getSerializedValue();
    }

    @JsonIgnore
    public String getStatusCodeRaw() {
        return statusCode;
    }

    @JsonIgnore
    public String getPaymentSubTypeRaw() {
        return paymentSubType;
    }

    @JsonIgnore
    public String getPaymentSubTypeExtensionRaw() {
        return paymentSubTypeExtension;
    }

    public void setPaymentSubType(Payment.SubType paymentSubType) {
        this.paymentSubType = paymentSubType.getSerializedValue();
    }

    public String getMessageRow() {
        return messageRow;
    }

    public void setMessageRow(String messageRow) {
        this.messageRow = messageRow;
    }

    public String getDueDateType() {
        return dueDateType;
    }

    public void setDueDateType(String dueDateType) {
        this.dueDateType = dueDateType;
    }

    public void setDueDateTypeDueDatePayment() {
        this.dueDateType = "DueDatePayment";
    }

    public Boolean getRecurringContinuously() {
        return recurringContinuously;
    }

    public void setRecurringContinuously(Boolean recurringContinuously) {
        this.recurringContinuously = recurringContinuously;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

    public String getBeneficiaryName() {
        return beneficiaryName;
    }

    public void setBeneficiaryName(String beneficiaryName) {
        this.beneficiaryName = beneficiaryName;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public Integer getRecurringNumberOfPayments() {
        return recurringNumberOfPayments;
    }

    public void setRecurringNumberOfPayments(Integer recurringNumberOfPayments) {
        this.recurringNumberOfPayments = recurringNumberOfPayments;
    }

    public void setPaymentSubTypeExtension(Payment.SubTypeExtension paymentSubTypeExtension) {
        this.paymentSubTypeExtension = paymentSubTypeExtension.getSerializedValue();
    }

    public String getRecurringFrequency() {
        return recurringFrequency;
    }

    public void setRecurringFrequency(String recurringFrequency) {
        this.recurringFrequency = recurringFrequency;
    }

    public void setRecurringFrequencyOnce() {
        this.recurringFrequency = "Once";
    }

    public String getToAccountId() {
        return toAccountId;
    }

    public void setToAccountId(String toAccountId) {
        this.toAccountId = toAccountId;
    }

    public String getFromAccountId() {
        return fromAccountId;
    }

    public void setFromAccountId(String fromAccountId) {
        this.fromAccountId = fromAccountId;
    }

    public String getReceiptCode() {
        return receiptCode;
    }

    public void setReceiptCode(String receiptCode) {
        this.receiptCode = receiptCode;
    }

    public void setReceiptCodeNoReceipt() {
        this.receiptCode = "NoReceipt";
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Boolean getAddBeneficiary() {
        return addBeneficiary;
    }

    public void setAddBeneficiary(Boolean addBeneficiary) {
        this.addBeneficiary = addBeneficiary;
    }

    public Boolean getStorePayment() {
        return storePayment;
    }

    public void setStorePayment(Boolean storePayment) {
        this.storePayment = storePayment;
    }

    public String getBeneficiaryNickName() {
        return beneficiaryNickName;
    }

    public void setBeneficiaryNickName(String beneficiaryNickName) {
        this.beneficiaryNickName = beneficiaryNickName;
    }
    /*
    @JsonSerialize(using = NordeaHashMapSerializer.String.class)
    private String toBranchNumber;

    @JsonSerialize(using = NordeaHashMapSerializer.String.class)
    private String category;

    @JsonSerialize(using = NordeaHashMapSerializer.String.class)
    private String reference;

    @JsonSerialize(using = NordeaHashMapSerializer.String.class)
    private String challenge;

    @JsonSerialize(using = NordeaHashMapSerializer.String.class)
    private String confirmationCode;

    @JsonSerialize(using = NordeaHashMapSerializer.String.class)
    private String personalNote;

    @JsonSerialize(using = NordeaHashMapSerializer.String.class)
    private String invoicePaymentType;

    @JsonSerialize(using = NordeaHashMapSerializer.String.class)
    private String giroNumber;

    @JsonSerialize(using = NordeaHashMapSerializer.String.class)
    private String encryptedPaymentData;

    @JsonSerialize(using = NordeaHashMapSerializer.String.class)
    private String beneficiaryBankId;
    */
}
