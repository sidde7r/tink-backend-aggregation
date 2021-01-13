package se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.fetcher.entity;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public final class IngAssociatedAccount {

    private String productNumber;
    private String uuid;

    public String getProductNumber() {
        return productNumber;
    }

    public String getUuid() {
        return uuid;
    }
}
