package se.tink.backend.rpc;

import io.swagger.annotations.ApiModelProperty;

import java.util.List;
import se.tink.backend.core.Statistic;
import se.tink.backend.core.Transaction;

public class SimilarTransactionsResponse {
    @ApiModelProperty(name = "statistics", value="Statistics of type 'income-and-expenses-and-transfers' for the similar transactions.", required = true)
    protected List<Statistic> statistics;
    @ApiModelProperty(name = "transactions", value="List of similar transactions.", required = true)
    protected List<Transaction> transactions;

    public List<Statistic> getStatistics() {
        return statistics;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public void setStatistics(List<Statistic> statistics) {
        this.statistics = statistics;
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }
}
