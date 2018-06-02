package se.tink.backend.aggregation.agents.banks.nordea.v18.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AccountTransactionsEntity {
    private Map<String, Object> accountId = new HashMap<String, Object>();
    private Map<String, Object> continueKey = new HashMap<String, Object>();
    private List<TransactionEntity> accountTransactions;

    public Map<String, Object> getContinueKey() {
        return continueKey;
    }

    public void setContinueKey(Map<String, Object> continueKey) {
        this.continueKey = continueKey;
    }

    public Map<String, Object> getAccountId() {
        return accountId;
    }

    public void setAccountId(Map<String, Object> accountId) {
        this.accountId = accountId;
    }

    public List<TransactionEntity> getAccountTransactions() {
        return accountTransactions;
    }

    public void setAccountTransaction(List<TransactionEntity> accountTransactions) {
        this.accountTransactions = accountTransactions;
    }
}
