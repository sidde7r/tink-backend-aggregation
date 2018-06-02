package se.tink.backend.rpc;

import io.protostuff.Exclude;
import io.protostuff.Tag;

import java.util.List;

import se.tink.backend.core.MerchantCluster;

public class SuggestMerchantizeResponse {

    @Exclude
    public static final int CACHE_EXPIRY = 30 * 60;
    @Exclude
    public static final String CACHE_PREFIX_USER = "merchantification-suggest-by-userId:";

    @Tag(1)
    private String clusterCategoryId;
    @Tag(2)
    private List<MerchantCluster> clusters;
    @Tag(3)
    private double merchantificationImprovement;
    @Tag(4)
    private double merchantificationLevel;

    public String getClusterCategoryId() {
        return clusterCategoryId;
    }

    public List<MerchantCluster> getClusters() {
        return clusters;
    }

    public double getMerchantificationImprovement() {
        return merchantificationImprovement;
    }

    public double getMerchantificationLevel() {
        return merchantificationLevel;
    }

    public void setClusterCategoryId(String clusterCategory) {
        this.clusterCategoryId = clusterCategory;
    }

    public void setClusters(List<MerchantCluster> clusters) {
        this.clusters = clusters;
    }

    public void setMerchantificationImprovement(double merchantificationImprovement) {
        this.merchantificationImprovement = merchantificationImprovement;
    }

    public void setMerchantificationLevel(double merchantificationLevel) {
        this.merchantificationLevel = merchantificationLevel;
    }
}
