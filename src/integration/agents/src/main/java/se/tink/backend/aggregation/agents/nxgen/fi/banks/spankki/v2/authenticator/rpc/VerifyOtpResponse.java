package se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.rpc.SpankkiResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class VerifyOtpResponse extends SpankkiResponse {
    @JsonProperty private String activationCode = "";

    @JsonIgnore
    public String getActivationCode() {
        return activationCode;
    }
}
