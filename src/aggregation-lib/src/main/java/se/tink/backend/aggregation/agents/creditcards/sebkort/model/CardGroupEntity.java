package se.tink.backend.aggregation.agents.creditcards.sebkort.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CardGroupEntity {
    protected String maskedCardNumber;
    protected String nameOnCard;
    protected String total;
    protected double totalNumber;
    protected List<TransactionGroupEntity> transactionGroups;

    public String getMaskedCardNumber() {
        return maskedCardNumber;
    }

    public String getNameOnCard() {
        return nameOnCard;
    }

    public String getTotal() {
        return total;
    }

    public double getTotalNumber() {
        return totalNumber;
    }

    public List<TransactionGroupEntity> getTransactionGroups() {
        return transactionGroups;
    }

    public void setMaskedCardNumber(String maskedCardNumber) {
        this.maskedCardNumber = maskedCardNumber;
    }

    public void setNameOnCard(String nameOnCard) {
        this.nameOnCard = nameOnCard;
    }

    public void setTotal(String total) {
        this.total = total;
    }

    public void setTotalNumber(double totalNumber) {
        this.totalNumber = totalNumber;
    }

    public void setTransactionGroups(List<TransactionGroupEntity> transactionGroups) {
        this.transactionGroups = transactionGroups;
    }

}
