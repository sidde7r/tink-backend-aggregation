package se.tink.libraries.identity.model;

import java.util.Date;

public class OutstandingDebt {
    private Date createdDate;
    private double amount;
    private int number;
    private Date registeredDate;

    private OutstandingDebt(double amount, int number, Date registeredDate, Date createdDate) {
        this.amount = amount;
        this.number = number;
        this.registeredDate = registeredDate;
        this.createdDate = createdDate;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public double getAmount() {
        return amount;
    }

    public int getNumber() {
        return number;
    }

    public Date getRegisteredDate() {
        return registeredDate;
    }

    public static OutstandingDebt of(double amount, int number, Date registeredDate, Date createdDate) {
        return new OutstandingDebt(amount, number, registeredDate, createdDate);
    }
}
