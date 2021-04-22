package se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class OtpEntity {

    @JsonProperty("bharosaSessionId")
    private String sessionId;

    private String otpCode;

    public OtpEntity(String otpCode, String sessionId) {
        this.otpCode = otpCode;
        this.sessionId = sessionId;
    }
}
