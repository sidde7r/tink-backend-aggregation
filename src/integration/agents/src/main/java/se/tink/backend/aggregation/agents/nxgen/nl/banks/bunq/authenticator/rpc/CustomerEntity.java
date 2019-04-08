package se.tink.backend.aggregation.agents.nxgen.nl.banks.bunq.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CustomerEntity {
    private int id;
    private String created;
    private String updated;

    @JsonProperty("billing_account_id")
    private Integer billingAccountId;

    public int getId() {
        return id;
    }

    public String getCreated() {
        return created;
    }

    public String getUpdated() {
        return updated;
    }

    public Integer getBillingAccountId() {
        return billingAccountId;
    }
}
