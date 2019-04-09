package se.tink.backend.aggregation.agents.creditcards.americanexpress.v3.model;

import java.util.List;

public class TransactionsRequest {
    private List<String> billingIndexList;
    private int sortedIndex;

    public int getSortedIndex() {
        return sortedIndex;
    }

    public void setSortedIndex(int sortedIndex) {
        this.sortedIndex = sortedIndex;
    }

    public List<String> getBillingIndexList() {
        return billingIndexList;
    }

    public void setBillingIndexList(List<String> billingIndexList) {
        this.billingIndexList = billingIndexList;
    }
}
