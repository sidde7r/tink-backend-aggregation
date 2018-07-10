package se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.fetcher.account.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ProductIdEntity {
    private String identifier;
    private String productBranch;
    private String currency;
    private String productType;

    public String getIdentifier() {
        return identifier;
    }

    public String getProductBranch() {
        return productBranch;
    }

    public String getCurrency() {
        return currency;
    }

    public String getProductType() {
        return productType;
    }
}
