package se.tink.backend.aggregation.agents.banks.nordea.v20.model.payments;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import se.tink.backend.aggregation.agents.banks.nordea.NordeaHashMapSerializer;

public class CreatePaymentIn {

    @JsonSerialize(using = NordeaHashMapSerializer.class)
    private String amount;

    @JsonSerialize(using = NordeaHashMapSerializer.class)
    private String toAccountIdNickname;

    @JsonSerialize(using = NordeaHashMapSerializer.class)
    private String currency;

    @JsonSerialize(using = NordeaHashMapSerializer.class)
    private String recurringContinuously;

    @JsonSerialize(using = NordeaHashMapSerializer.class)
    private String messageRow;

    @JsonSerialize(using = NordeaHashMapSerializer.class)
    private String reference;

    @JsonSerialize(using = NordeaHashMapSerializer.class)
    private String paymentSubType;

    @JsonSerialize(using = NordeaHashMapSerializer.class)
    private String personalNote;

    @JsonSerialize(using = NordeaHashMapSerializer.class)
    private String dueDateType;

    @JsonSerialize(using = NordeaHashMapSerializer.class)
    private String beneficiaryName;

    @JsonSerialize(using = NordeaHashMapSerializer.class)
    private String recurringNumberOfPayments;

    @JsonSerialize(using = NordeaHashMapSerializer.class)
    private String statusCode;

    @JsonSerialize(using = NordeaHashMapSerializer.class)
    private String category;

    @JsonSerialize(using = NordeaHashMapSerializer.class)
    private String toBranchNumber;

    @JsonSerialize(using = NordeaHashMapSerializer.class)
    private String invoicePaymentId;

    @JsonSerialize(using = NordeaHashMapSerializer.class)
    private String giroNumber;

    @JsonSerialize(using = NordeaHashMapSerializer.class)
    private String scannerUsed;

    @JsonSerialize(using = NordeaHashMapSerializer.class)
    private String fromAccountBranchId;

    @JsonSerialize(using = NordeaHashMapSerializer.class)
    private String receiptCode;

    @JsonSerialize(using = NordeaHashMapSerializer.class)
    private String toAccountNumber;

    @JsonSerialize(using = NordeaHashMapSerializer.class)
    private String encryptedPaymentData;

    @JsonSerialize(using = NordeaHashMapSerializer.class)
    private String fromAccountProductTypeExtension;

    @JsonSerialize(using = NordeaHashMapSerializer.class)
    private String addBeneficiary;

    @JsonSerialize(using = NordeaHashMapSerializer.class)
    private String fromAccountIdNickname;

    @JsonSerialize(using = NordeaHashMapSerializer.class)
    private String paymentSubTypeExtension;

    @JsonSerialize(using = NordeaHashMapSerializer.class)
    private String recurringFrequency;

    @JsonSerialize(using = NordeaHashMapSerializer.class)
    private String dueDate;

    @JsonSerialize(using = NordeaHashMapSerializer.class)
    private String storePayment;

    @JsonSerialize(using = NordeaHashMapSerializer.class)
    private String beneficiaryNickName;

    @JsonSerialize(using = NordeaHashMapSerializer.class)
    private String confirmationCode;

    @JsonSerialize(using = NordeaHashMapSerializer.class)
    private String fromAccountId;

    @JsonSerialize(using = NordeaHashMapSerializer.class)
    private String beneficiaryBankId;

    @JsonSerialize(using = NordeaHashMapSerializer.class)
    private String challenge;

    @JsonSerialize(using = NordeaHashMapSerializer.class)
    private String potentialFraud;

    @JsonSerialize(using = NordeaHashMapSerializer.class)
    private String invoicePaymentType;

    @JsonSerialize(using = NordeaHashMapSerializer.class)
    private String toAccountId;

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getToAccountIdNickname() {
        return toAccountIdNickname;
    }

    public void setToAccountIdNickname(String toAccountIdNickname) {
        this.toAccountIdNickname = toAccountIdNickname;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getRecurringContinuously() {
        return recurringContinuously;
    }

    public void setRecurringContinuously(String recurringContinuously) {
        this.recurringContinuously = recurringContinuously;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getPersonalNote() {
        return personalNote;
    }

    public void setPersonalNote(String personalNote) {
        this.personalNote = personalNote;
    }

    public String getDueDateType() {
        return dueDateType;
    }

    public void setDueDateType(String dueDateType) {
        this.dueDateType = dueDateType;
    }

    public String getBeneficiaryName() {
        return beneficiaryName;
    }

    public void setBeneficiaryName(String beneficiaryName) {
        this.beneficiaryName = beneficiaryName;
    }

    public String getRecurringNumberOfPayments() {
        return recurringNumberOfPayments;
    }

    public void setRecurringNumberOfPayments(String recurringNumberOfPayments) {
        this.recurringNumberOfPayments = recurringNumberOfPayments;
    }

    public String getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getToBranchNumber() {
        return toBranchNumber;
    }

    public void setToBranchNumber(String toBranchNumber) {
        this.toBranchNumber = toBranchNumber;
    }

    public String getInvoicePaymentId() {
        return invoicePaymentId;
    }

    public void setInvoicePaymentId(String invoicePaymentId) {
        this.invoicePaymentId = invoicePaymentId;
    }

    public String getScannerUsed() {
        return scannerUsed;
    }

    public void setScannerUsed(String scannerUsed) {
        this.scannerUsed = scannerUsed;
    }

    public String getFromAccountBranchId() {
        return fromAccountBranchId;
    }

    public void setFromAccountBranchId(String fromAccountBranchId) {
        this.fromAccountBranchId = fromAccountBranchId;
    }

    public String getReceiptCode() {
        return receiptCode;
    }

    public void setReceiptCode(String receiptCode) {
        this.receiptCode = receiptCode;
    }

    public String getToAccountNumber() {
        return toAccountNumber;
    }

    public void setToAccountNumber(String toAccountNumber) {
        this.toAccountNumber = toAccountNumber;
    }

    public String getEncryptedPaymentData() {
        return encryptedPaymentData;
    }

    public void setEncryptedPaymentData(String encryptedPaymentData) {
        this.encryptedPaymentData = encryptedPaymentData;
    }

    public String getFromAccountProductTypeExtension() {
        return fromAccountProductTypeExtension;
    }

    public void setFromAccountProductTypeExtension(String fromAccountProductTypeExtension) {
        this.fromAccountProductTypeExtension = fromAccountProductTypeExtension;
    }

    public String getAddBeneficiary() {
        return addBeneficiary;
    }

    public void setAddBeneficiary(String addBeneficiary) {
        this.addBeneficiary = addBeneficiary;
    }

    public String getFromAccountIdNickname() {
        return fromAccountIdNickname;
    }

    public void setFromAccountIdNickname(String fromAccountIdNickname) {
        this.fromAccountIdNickname = fromAccountIdNickname;
    }

    public String getPaymentSubTypeExtension() {
        return paymentSubTypeExtension;
    }

    public void setPaymentSubTypeExtension(String paymentSubTypeExtension) {
        this.paymentSubTypeExtension = paymentSubTypeExtension;
    }

    public String getRecurringFrequency() {
        return recurringFrequency;
    }

    public void setRecurringFrequency(String recurringFrequency) {
        this.recurringFrequency = recurringFrequency;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public String getStorePayment() {
        return storePayment;
    }

    public void setStorePayment(String storePayment) {
        this.storePayment = storePayment;
    }

    public String getBeneficiaryNickName() {
        return beneficiaryNickName;
    }

    public void setBeneficiaryNickName(String beneficiaryNickName) {
        this.beneficiaryNickName = beneficiaryNickName;
    }

    public String getConfirmationCode() {
        return confirmationCode;
    }

    public void setConfirmationCode(String confirmationCode) {
        this.confirmationCode = confirmationCode;
    }

    public String getFromAccountId() {
        return fromAccountId;
    }

    public void setFromAccountId(String fromAccountId) {
        this.fromAccountId = fromAccountId;
    }

    public String getBeneficiaryBankId() {
        return beneficiaryBankId;
    }

    public void setBeneficiaryBankId(String beneficiaryBankId) {
        this.beneficiaryBankId = beneficiaryBankId;
    }

    public String getChallenge() {
        return challenge;
    }

    public void setChallenge(String challenge) {
        this.challenge = challenge;
    }

    public String getPotentialFraud() {
        return potentialFraud;
    }

    public void setPotentialFraud(String potentialFraud) {
        this.potentialFraud = potentialFraud;
    }

    public String getInvoicePaymentType() {
        return invoicePaymentType;
    }

    public void setInvoicePaymentType(String invoicePaymentType) {
        this.invoicePaymentType = invoicePaymentType;
    }

    public String getToAccountId() {
        return toAccountId;
    }

    public void setToAccountId(String toAccountId) {
        this.toAccountId = toAccountId;
    }

    public String getPaymentSubType() {
        return paymentSubType;
    }

    public void setPaymentSubType(String paymentSubType) {
        this.paymentSubType = paymentSubType;
    }

    public String getMessageRow() {
        return messageRow;
    }

    public void setMessageRow(String messageRow) {
        this.messageRow = messageRow;
    }

    public String getGiroNumber() {
        return giroNumber;
    }

    public void setGiroNumber(String giroNumber) {
        this.giroNumber = giroNumber;
    }
}
