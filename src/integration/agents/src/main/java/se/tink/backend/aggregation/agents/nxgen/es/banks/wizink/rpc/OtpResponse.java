package se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class OtpResponse {
    private String otpCode;
    private String bharosaSessionId;
    private String otpGeneratedCode;

    public String getBharosaSessionId() {
        return bharosaSessionId;
    }
}
