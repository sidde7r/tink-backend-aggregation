package se.tink.backend.aggregation.agents.banks.sbab.model.response;

import com.google.api.client.util.Lists;
import java.util.List;
import se.tink.backend.aggregation.agents.models.Transaction;

public class FetchTransactionsResponse {

    private List<Transaction> transactions = Lists.newArrayList();
    private List<Transaction> upcomingTransactions = Lists.newArrayList();
    private boolean hasMoreResults;
    private String token;
    private String strutsTokenName;
    private String postUrl;

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }

    public boolean hasMoreResults() {
        return hasMoreResults;
    }

    public void setHasMoreResults(boolean hasMoreResults) {
        this.hasMoreResults = hasMoreResults;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getStrutsTokenName() {
        return strutsTokenName;
    }

    public void setStrutsTokenName(String strutsTokenName) {
        this.strutsTokenName = strutsTokenName;
    }

    public String getPostUrl() {
        return postUrl;
    }

    public void setPostUrl(String postUrl) {
        this.postUrl = postUrl;
    }

    public void setUpcomingTransactions(List<Transaction> upcomingTransactions) {
        this.upcomingTransactions = upcomingTransactions;
    }

    public List<Transaction> getUpcomingTransactions() {
        return upcomingTransactions;
    }
}
