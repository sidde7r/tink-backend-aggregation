package se.tink.backend.core;

import com.fasterxml.jackson.annotation.JsonIgnore;


public class VenueStatistics extends Venue {

    protected int purchaseCount;
    protected int dayCount;
    protected int customersCount;
    protected double totalAmount;
    
    public VenueStatistics() {
    }
    
    public int getPurchaseCount() {
        return purchaseCount;
    }
    
    public int getDayCount() {
        return dayCount;
    }
    
    public int getCustomersCount() {
        return customersCount;
    }
    
    public double getTotalAmount() {
        return totalAmount;
    }
    
    @JsonIgnore
    public double getAmountPerPurchase() {
        return getTotalAmount() / (double) getPurchaseCount();
    }
    
    @JsonIgnore
    public double getAmountPerDay() {
        return getTotalAmount() / (double) getDayCount();
    }
    
    @JsonIgnore
    public double getPurchasesPerDay() {
        return (double) getPurchaseCount() / (double) getDayCount();
    }
    
    @JsonIgnore
    public double getPurchasesPerCustomer() {
        return (double) getPurchaseCount() / (double) getCustomersCount();
    }
    
    public void setPurchaseCount(int purchaseCount) {
        this.purchaseCount = purchaseCount;
    }
    
    public void setDayCount(int dayCount) {
        this.dayCount = dayCount;
    }
    
    public void setCustomersCount(int customersCount) {
        this.customersCount = customersCount;
    }
    
    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }
}