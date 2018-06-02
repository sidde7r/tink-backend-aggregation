package se.tink.backend.abnamro.workers.activity.generators.models;

import java.util.Date;
import java.util.List;
import se.tink.backend.core.Transaction;

public class AbnAmroAutomaticSavingsSummaryActivityData {

    private double amount;
    private int count;
    private Date endDate;
    private Date startDate;
    private List<Transaction> transactions;
    
    public double getAmount() {
        return amount;
    }
    
    public int getCount() {
        return count;
    }
    
    public Date getEndDate() {
        return endDate;
    }
    
    public Date getStartDate() {
        return startDate;
    }
    
    public List<Transaction> getTransactions() {
        return transactions;
    }
    
    public void setAmount(double amount) {
        this.amount = amount;
    }
    
    public void setCount(int count) {
        this.count = count;
    }
    
    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }
    
    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }
    
    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }
}
