package se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.fetcher.card.entity;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransactionsItem {

    private String href;

    public String getHref() {
        return href;
    }
}
