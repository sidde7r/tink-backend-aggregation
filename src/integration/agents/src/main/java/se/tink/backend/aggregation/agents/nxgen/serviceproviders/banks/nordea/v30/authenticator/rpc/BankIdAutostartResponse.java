package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class BankIdAutostartResponse {
    private String status;
    private String code;

    @JsonProperty("session_id")
    private String sessionId;

    @JsonProperty("auto_start_token")
    private String autoStartToken;

    @JsonProperty("verify_after")
    private int verifyAfter;
}
