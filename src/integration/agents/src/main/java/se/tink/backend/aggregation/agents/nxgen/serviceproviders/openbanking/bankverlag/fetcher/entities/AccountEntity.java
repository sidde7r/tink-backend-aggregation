package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankverlag.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountEntity {
    private String resourceId;
    private String iban;
    private String currency;
    private String product;
    private String ownerName;
    private String name;
    private String cashAccountType;

    @JsonProperty("_links")
    private LinksEntity links;

    public String getResourceId() {
        return resourceId;
    }

    public String getIban() {
        return iban;
    }

    public String getCurrency() {
        return currency;
    }

    public String getProduct() {
        return product;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public String getName() {
        return name;
    }

    public String getCashAccountType() {
        return cashAccountType;
    }
}
