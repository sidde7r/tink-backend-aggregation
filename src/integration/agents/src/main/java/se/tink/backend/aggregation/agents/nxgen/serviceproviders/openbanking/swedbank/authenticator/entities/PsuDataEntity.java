package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PsuDataEntity {

    @JsonProperty("bankID")
    private String bankId;

    public PsuDataEntity(String bankId) {
        this.bankId = bankId;
    }
}
