package se.tink.backend.core;

public class SuggestMerchantsActivityData {
    private String clusterCategoryId;
    private double merchantificationImprovement;
    private double merchantificationLevel;

    public String getClusterCategoryId() {
        return clusterCategoryId;
    }

    public double getMerchantificationImprovement() {
        return merchantificationImprovement;
    }

    public double getMerchantificationLevel() {
        return merchantificationLevel;
    }

    public void setClusterCategoryId(String clusterCategoryId) {
        this.clusterCategoryId = clusterCategoryId;
    }

    public void setMerchantificationImprovement(double merchantificationImprovement) {
        this.merchantificationImprovement = merchantificationImprovement;
    }

    public void setMerchantificationLevel(double merchantificationLevel) {
        this.merchantificationLevel = merchantificationLevel;
    }
}