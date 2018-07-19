package se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.fetcher.transaction.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class Identifier {
    private String productType;
    private String currency;
    private String identifier;
    private String productBranch;

    public Identifier(String productType, String currency, String identifier, String productBranch) {
        this.productType = productType;
        this.currency = currency;
        this.identifier = identifier;
        this.productBranch = productBranch;
    }
}
