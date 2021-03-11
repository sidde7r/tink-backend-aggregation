package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccessEntity {

    @JsonProperty private String allPsd2;

    public AccessEntity(String allPsd2) {
        this.allPsd2 = allPsd2;
    }

    public AccessEntity() {}
}
