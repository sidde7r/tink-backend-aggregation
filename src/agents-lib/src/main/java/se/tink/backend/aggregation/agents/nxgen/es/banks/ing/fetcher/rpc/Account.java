package se.tink.backend.aggregation.agents.nxgen.es.banks.ing.fetcher.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public final class Account {

    private String productNumber;
    private String uuid;

    public String getProductNumber() {
        return productNumber;
    }

    public void setProductNumber(String productNumber) {
        this.productNumber = productNumber;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}
