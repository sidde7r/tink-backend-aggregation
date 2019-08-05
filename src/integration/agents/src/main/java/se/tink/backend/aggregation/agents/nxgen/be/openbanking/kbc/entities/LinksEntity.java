package se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LinksEntity {

    @JsonProperty("account")
    private String account;

    private String balances;

    public String getAccount() {
        return account;
    }
}
