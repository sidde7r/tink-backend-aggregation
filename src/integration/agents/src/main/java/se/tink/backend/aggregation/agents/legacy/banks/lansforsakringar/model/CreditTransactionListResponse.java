package se.tink.backend.aggregation.agents.banks.lansforsakringar.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CreditTransactionListResponse {
    private boolean morePages;
    protected List<CardTransactionEntity> transactions;

    public List<CardTransactionEntity> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<CardTransactionEntity> transactions) {
        this.transactions = transactions;
    }

    public boolean hasMorePages() {
        return morePages;
    }

    public void setMorePages(boolean morePages) {
        this.morePages = morePages;
    }
}
