package se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.fetcher.account.entities;

import com.google.api.client.repackaged.com.google.common.base.Preconditions;
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
        Preconditions.checkState(
                productType != null, "Expected a Product type object but it was null");
        return productType;
    }
}
