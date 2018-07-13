package se.tink.backend.aggregation.agents.banks.swedbank.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import se.tink.backend.aggregation.utils.TrimmingStringDeserializer;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CardAccountEntity {
    protected String availableAmount;
    protected String cardNumber;
    protected String creditLimit;
    protected String currentBalance;
    protected String name;
    protected String reservedAmount;

    public String getAvailableAmount() {
        return availableAmount;
    }

    @JsonDeserialize(using = TrimmingStringDeserializer.class)
    public void setAvailableAmount(String availableAmount) {
        this.availableAmount = availableAmount;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    @JsonDeserialize(using = TrimmingStringDeserializer.class)
    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getCreditLimit() {
        return creditLimit;
    }

    @JsonDeserialize(using = TrimmingStringDeserializer.class)
    public void setCreditLimit(String creditLimit) {
        this.creditLimit = creditLimit;
    }

    public String getCurrentBalance() {
        return currentBalance;
    }

    @JsonDeserialize(using = TrimmingStringDeserializer.class)
    public void setCurrentBalance(String currentBalance) {
        this.currentBalance = currentBalance;
    }

    public String getName() {
        return name;
    }

    @JsonDeserialize(using = TrimmingStringDeserializer.class)
    public void setName(String name) {
        this.name = name;
    }

    public String getReservedAmount() {
        return reservedAmount;
    }

    @JsonDeserialize(using = TrimmingStringDeserializer.class)
    public void setReservedAmount(String reservedAmount) {
        this.reservedAmount = reservedAmount;
    }
}
