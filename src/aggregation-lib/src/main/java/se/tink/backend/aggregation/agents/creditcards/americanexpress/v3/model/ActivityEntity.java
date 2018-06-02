package se.tink.backend.aggregation.agents.creditcards.americanexpress.v3.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ActivityEntity {
    private List<TransactionEntity> transactionList;
    private String billingIndex;
    private String statementBalance;

    public List<TransactionEntity> getTransactionList() {
        return transactionList;
    }

    public void setTransactionList(List<TransactionEntity> transactionList) {
        this.transactionList = transactionList;
    }

    public String getBillingIndex() {
        return billingIndex;
    }

    public void setBillingIndex(String billingIndex) {
        this.billingIndex = billingIndex;
    }

    public String getStatementBalance() {
        return statementBalance;
    }

    public void setStatementBalance(String statementBalance) {
        this.statementBalance = statementBalance;
    }

}
