package se.tink.backend.aggregation.agents.nxgen.se.banks.volvofinans.authenticator.rpc.bankid;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class InitBankIdRequest {

    @JsonProperty("identitet")
    private String  ssn;

    public InitBankIdRequest(String ssn) {
        this.ssn = ssn;
    }
}
