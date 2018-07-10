package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v45.fetcher.rpc;

import java.util.List;

public class TransactionsRequest {

    private List<String> billingIndexList;
    private int sortedIndex;

    public void setSortedIndex(int sortedIndex) {
        this.sortedIndex = sortedIndex;
    }

    public void setBillingIndexList(List<String> billingIndexList) {
        this.billingIndexList = billingIndexList;
    }

    public List<String> getBillingIndexList() {
        return billingIndexList;
    }

    public int getSortedIndex() {
        return sortedIndex;
    }
}
