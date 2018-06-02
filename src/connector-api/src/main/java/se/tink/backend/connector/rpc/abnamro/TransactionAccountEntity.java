package se.tink.backend.connector.rpc.abnamro;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.base.MoreObjects;
import java.util.Collections;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TransactionAccountEntity {

    private long accountNumber;
    private double balance;
    private int status;
    private List<TransactionEntity> transactions;

    private List<Long> bcNumbers;

    public long getAccountNumber() {
        return accountNumber;
    }
    
    public double getBalance() {
        return balance;
    }
    
    public int getStatus() {
        return status;
    }
    
    public List<TransactionEntity> getTransactions() {
        return transactions;
    }

    public void setAccountNumber(long accountNumber) {
        this.accountNumber = accountNumber;
    }
    
    public void setBalance(double balance) {
        this.balance = balance;
    }
    
    public void setStatus(int status) {
        this.status = status;
    }
    
    public void setTransactions(List<TransactionEntity> transactions) {
        this.transactions = transactions;
    }

    public List<Long> getBcNumbers() {
        return bcNumbers == null ? Collections.emptyList() : bcNumbers;
    }

    public void setBcNumbers(List<Long> bcNumbers) {
        this.bcNumbers = bcNumbers;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("bcNumbers", bcNumbers)
                .add("accountNumber", accountNumber)
                .add("balance", balance)
                .add("status", status)
                .add("transactions-size", transactions.size())
                .toString();
    }
}
