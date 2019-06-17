package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccessEntity {

    @JsonProperty
    private String allPsd2;
    @JsonProperty
    private String allAvailableAccounts;

    public AccessEntity(String allPsd2, String allAvailableAccounts) {
        this.allPsd2 = allPsd2;
        this.allAvailableAccounts = allAvailableAccounts;
    }

    public AccessEntity() {}

    @JsonIgnore
    public void setAllPsd2(String allPsd2) {
        this.allPsd2 = allPsd2;
    }

    @JsonIgnore
    public void setAllAvailableAccounts(String allAvailableAccounts) {
        this.allAvailableAccounts = allAvailableAccounts;
    }
}
