package se.tink.backend.aggregation.agents.creditcards.sebkort.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SummaryTransactionsEntity {
    protected String title;
    protected String total;
    protected String type;
    protected double totalNumber;
    protected List<TransactionEntity> transactions;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTotal() {
        return total;
    }

    public void setTotal(String total) {
        this.total = total;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getTotalNumber() {
        return totalNumber;
    }

    public void setTotalNumber(double totalNumber) {
        this.totalNumber = totalNumber;
    }

    public List<TransactionEntity> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<TransactionEntity> transactions) {
        this.transactions = transactions;
    }
}
