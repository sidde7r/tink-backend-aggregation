package se.tink.backend.aggregation.agents.nxgen.es.banks.ing.fetcher.rpc;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public final class Element {

    private String uuid;
    private String productNumber;
    private int sequence;
    private double balance;
    private String tranCode;
    private String comment;
    private String description;
    private double amount;
    private int categoryId;
    private boolean hasUncertainCategorization;
    private String effectiveDate;
    private Status status;
    private String cardNumber;
    private String operationId;
    private String crossingCode;
    private List<DetectedCategory> detectedCategories = null;

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getOperationId() {
        return operationId;
    }

    public void setOperationId(String operationId) {
        this.operationId = operationId;
    }

    public String getCrossingCode() {
        return crossingCode;
    }

    public void setCrossingCode(String crossingCode) {
        this.crossingCode = crossingCode;
    }

    public List<DetectedCategory> getDetectedCategories() {
        return detectedCategories;
    }

    public void setDetectedCategories(List<DetectedCategory> detectedCategories) {
        this.detectedCategories = detectedCategories;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getProductNumber() {
        return productNumber;
    }

    public void setProductNumber(String productNumber) {
        this.productNumber = productNumber;
    }

    public int getSequence() {
        return sequence;
    }

    public void setSequence(int sequence) {
        this.sequence = sequence;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public String getTranCode() {
        return tranCode;
    }

    public void setTranCode(String tranCode) {
        this.tranCode = tranCode;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public boolean isHasUncertainCategorization() {
        return hasUncertainCategorization;
    }

    public void setHasUncertainCategorization(boolean hasUncertainCategorization) {
        this.hasUncertainCategorization = hasUncertainCategorization;
    }

    public String getEffectiveDate() {
        return effectiveDate;
    }

    public void setEffectiveDate(String effectiveDate) {
        this.effectiveDate = effectiveDate;
    }
}
