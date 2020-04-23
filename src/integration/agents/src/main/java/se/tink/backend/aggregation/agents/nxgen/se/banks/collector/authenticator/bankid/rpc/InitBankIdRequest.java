package se.tink.backend.aggregation.agents.nxgen.se.banks.collector.authenticator.bankid.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class InitBankIdRequest {
    @JsonProperty("personal_number")
    private String ssn;

    public InitBankIdRequest(String ssn) {
        this.ssn = ssn;
    }
}
