package se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.fetchers.credit.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CreditCardEntity {
    @JsonProperty("AccountType")
    private String accountType;
    @JsonProperty("CustomerNumber")
    private String customerNumber;
    @JsonProperty("Active")
    private boolean active;

    public String getAccountType() {
        return accountType;
    }

    public String getCustomerNumber() {
        return customerNumber;
    }

    public boolean isActive() {
        return active;
    }
}
