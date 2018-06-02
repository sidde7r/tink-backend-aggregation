package se.tink.backend.core;

import java.util.Date;

public class FraudTransactionEntity {
    private String id;
    private Date date;
    private String description;
    private double amount;
    private String categoryId;

    public FraudTransactionEntity() {
    }
    
    public FraudTransactionEntity(Transaction transaction) {
        this.id = transaction.getId();
        this.date = transaction.getDate();
        this.description = transaction.getDescription();
        this.amount = transaction.getAmount();
        this.categoryId = transaction.getCategoryId();
    }
    
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
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

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }
}