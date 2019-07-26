package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransactionsRequest {

    private List<Integer> billingIndexList;
    private int sortedIndex;

    @JsonIgnore private boolean canStillFetch;

    public TransactionsRequest(List<Integer> billingIndexList, int sortedIndex) {
        this.billingIndexList = billingIndexList;
        this.sortedIndex = sortedIndex;
    }

    public List<Integer> getBillingIndexList() {
        return billingIndexList;
    }

    public int getSortedIndex() {
        return sortedIndex;
    }
}
