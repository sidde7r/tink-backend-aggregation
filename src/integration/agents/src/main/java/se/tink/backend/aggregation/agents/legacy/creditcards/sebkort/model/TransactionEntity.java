package se.tink.backend.aggregation.agents.creditcards.sebkort.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TransactionEntity {
    protected String amount;
    protected double amountNumber;
    protected String city;
    protected String currency;
    protected String description;
    protected String exchangeRateDescription;
    protected String originalAmountDate;
    protected String originalAmountOrVat;
    protected double originalAmountOrVatNumber;
    protected String postingDate;
    protected String refTransactionId;
    protected String transactionId;

    public String getAmount() {
        return amount;
    }

    public double getAmountNumber() {
        return amountNumber;
    }

    public String getCity() {
        return city;
    }

    public String getCurrency() {
        return currency;
    }

    public String getDescription() {
        return description;
    }

    public String getExchangeRateDescription() {
        return exchangeRateDescription;
    }

    public String getOriginalAmountDate() {
        return originalAmountDate;
    }

    public String getOriginalAmountOrVat() {
        return originalAmountOrVat;
    }

    public double getOriginalAmountOrVatNumber() {
        return originalAmountOrVatNumber;
    }

    public String getPostingDate() {
        return postingDate;
    }

    public String getRefTransactionId() {
        return refTransactionId;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public void setAmountNumber(double amountNumber) {
        this.amountNumber = amountNumber;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setExchangeRateDescription(String exchangeRateDescription) {
        this.exchangeRateDescription = exchangeRateDescription;
    }

    public void setOriginalAmountDate(String originalAmountDate) {
        this.originalAmountDate = originalAmountDate;
    }

    public void setOriginalAmountOrVat(String originalAmountOrVat) {
        this.originalAmountOrVat = originalAmountOrVat;
    }

    public void setOriginalAmountOrVatNumber(double originalAmountOrVatNumber) {
        this.originalAmountOrVatNumber = originalAmountOrVatNumber;
    }

    public void setPostingDate(String postingDate) {
        this.postingDate = postingDate;
    }

    public void setRefTransactionId(String refTransactionId) {
        this.refTransactionId = refTransactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }
}
