package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.fetchers.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransactionsRequest {
    private final String productId;
    private final int pageNr;

    public TransactionsRequest(String productId, int pageNr) {
        this.productId = productId;
        this.pageNr = pageNr;
    }
}
