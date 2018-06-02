package se.tink.backend.core;

import com.google.common.base.MoreObjects;
import io.swagger.annotations.ApiModelProperty;
import java.util.List;

public class TransactionCluster {
    
    @ApiModelProperty(name = "categorizationImprovement", value="The categorization improvement achived if cluster is categorized.", example = "0.003")
    private double categorizationImprovement;
    @ApiModelProperty(name = "description", value="A description of the cluster to categorized.", example = "McDonalds Stock")
    private String description;
    @ApiModelProperty(name = "score", hidden = true)
    private double score;
    @ApiModelProperty(name = "transactions", value="List of transactions belonging to this cluster.")
    private List<Transaction> transactions;

    public double getCategorizationImprovement() {
        return categorizationImprovement;
    }

    public String getDescription() {
        return description;
    }

    public double getScore() {
        return score;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public void setCategorizationImprovement(double categorizationImprovement) {
        this.categorizationImprovement = categorizationImprovement;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("description", description).add("score", score)
                .add("categorizationImprovement", categorizationImprovement).add("transactions", transactions)
                .toString();
    }
}
