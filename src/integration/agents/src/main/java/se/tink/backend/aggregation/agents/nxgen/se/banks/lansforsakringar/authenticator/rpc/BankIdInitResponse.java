package se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BankIdInitResponse {
    @JsonProperty private String reference;

    public String getReference() {
        return reference;
    }
}
