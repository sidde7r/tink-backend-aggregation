package se.tink.backend.core;

import io.protostuff.Tag;

import java.util.List;

public class MerchantCluster {

    @Tag(1)
    private String description;
    @Tag(2)
    private double merchantificationImprovement;
    @Tag(3)
    private List<Merchant> merchants;
    @Tag(4)
    private List<Transaction> transactions;

    public String getDescription() {
        return description;
    }

    public double getMerchantificationImprovement() {
        return merchantificationImprovement;
    }

    public List<Merchant> getMerchants() {
        return merchants;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setMerchantificationImprovement(double merchantificationImprovement) {
        this.merchantificationImprovement = merchantificationImprovement;
    }

    public void setMerchants(List<Merchant> merchants) {
        this.merchants = merchants;
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }

}
