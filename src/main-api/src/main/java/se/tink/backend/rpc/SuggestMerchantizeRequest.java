package se.tink.backend.rpc;

import io.protostuff.Tag;

public class SuggestMerchantizeRequest {

    @Tag(1)
    private String categoryId;
    @Tag(2)
    private int numberOfClusters;

    public String getCategoryId() {
        return categoryId;
    }

    public int getNumberOfClusters() {
        return numberOfClusters;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public void setNumberOfClusters(int numberOfClusters) {
        this.numberOfClusters = numberOfClusters;
    }
}
