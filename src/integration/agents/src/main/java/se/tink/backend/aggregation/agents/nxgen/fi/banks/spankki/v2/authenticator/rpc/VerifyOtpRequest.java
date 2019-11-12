package se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class VerifyOtpRequest {
    @JsonProperty private String verificationCode;

    public VerifyOtpRequest(String verificationCode) {
        this.verificationCode = verificationCode;
    }
}
