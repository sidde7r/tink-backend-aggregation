package se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.fetcher.entity;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CardTransactionEntity {
    private String href;

    public String getTransactionLink() {
        return href;
    }
}
