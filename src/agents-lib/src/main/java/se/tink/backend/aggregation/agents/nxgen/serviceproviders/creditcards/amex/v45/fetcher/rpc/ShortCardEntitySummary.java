package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v45.fetcher.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ShortCardEntitySummary {
    private String cardMemberName;
    private String cardProductName;
    private int sortedIndex;

    public int getSortedIndex() {
        return sortedIndex;
    }

    public String getCardProductName() {
        return cardProductName;
    }

}
