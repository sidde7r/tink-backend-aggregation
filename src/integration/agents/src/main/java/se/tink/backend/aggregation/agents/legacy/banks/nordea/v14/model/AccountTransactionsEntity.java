package se.tink.backend.aggregation.agents.banks.nordea.v14.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AccountTransactionsEntity {
    protected Map<String, Object> accountId = new HashMap<String, Object>();
    protected Map<String, Object> continueKey = new HashMap<String, Object>();
    protected List<TransactionEntity> accountTransactions;

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
