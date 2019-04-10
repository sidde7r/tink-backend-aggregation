package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransactionsRequest {

    private List<Integer> billingIndexList;
    private int sortedIndex;

    @JsonIgnore private boolean canStillFetch;

    public List<Integer> getBillingIndexList() {
        return billingIndexList;
    }

    public TransactionsRequest setBillingIndexList(List<Integer> billingIndexList) {
        this.billingIndexList = billingIndexList;
        return this;
    }

    public int getSortedIndex() {
        return sortedIndex;
    }

    public TransactionsRequest setSortedIndex(int sortedIndex) {
        this.sortedIndex = sortedIndex;
        return this;
    }
}
