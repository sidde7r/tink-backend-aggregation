package se.tink.backend.rpc;

import com.google.common.base.MoreObjects;
import io.swagger.annotations.ApiModelProperty;
import java.util.List;
import se.tink.backend.core.TransactionCluster;

public class SuggestTransactionsResponse {
    public static final int CACHE_EXPIRY = 30 * 60;

    @ApiModelProperty(name = "categorizationImprovement", value = "The categorization improvement achieve if all clusters are categorized.", example = "0.01", required = true)
    protected double categorizationImprovement;
    @ApiModelProperty(name = "categorizationLevel", value = "The current categorization level before categorization.", example = "0.93", required = true)
    protected double categorizationLevel;
    @ApiModelProperty(name = "clusters", value = "Clusters to categorize.", required = true)
    protected List<TransactionCluster> clusters;
    @ApiModelProperty(name = "numberOfClusters", hidden = true)
    protected int numberOfClusters;
    @ApiModelProperty(name = "numberOfTransactions", hidden = true)
    protected long numberOfTransactions;
    @ApiModelProperty(name = "numberOfUncategorizedTransactions", hidden = true)
    protected long numberOfUncategorizedTransactions;

    public double getCategorizationImprovement() {
        return categorizationImprovement;
    }

    public double getCategorizationLevel() {
        return categorizationLevel;
    }

    public List<TransactionCluster> getClusters() {
        return clusters;
    }

    public int getNumberOfClusters() {
        return numberOfClusters;
    }

    public long getNumberOfTransactions() {
        return numberOfTransactions;
    }

    public long getNumberOfUncategorizedTransactions() {
        return numberOfUncategorizedTransactions;
    }

    public void setCategorizationImprovement(double categorizationImprovement) {
        this.categorizationImprovement = categorizationImprovement;
    }

    public void setCategorizationLevel(double categorizationLevel) {
        this.categorizationLevel = categorizationLevel;
    }

    public void setClusters(List<TransactionCluster> clusters) {
        this.clusters = clusters;
    }

    public void setNumberOfClusters(int numberOfClusters) {
        this.numberOfClusters = numberOfClusters;
    }

    public void setNumberOfTransactions(long numberOfTransactions) {
        this.numberOfTransactions = numberOfTransactions;
    }

    public void setNumberOfUncategorizedTransactions(long numberOfUncategorizedTransactions) {
        this.numberOfUncategorizedTransactions = numberOfUncategorizedTransactions;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("numberOfTransactions", numberOfTransactions)
                .add("numberOfTransactions", numberOfTransactions).add("numberOfClusters", numberOfClusters)
                .add("categorizationLevel", categorizationLevel)
                .add("categorizationImprovement", categorizationImprovement).add("clusters", clusters).toString();
    }
}
