package se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class OtpRequest {

    @JsonProperty("Otp")
    private OtpRequestBody otpRequestBody;

    public OtpRequest(String otpCode, String bharosaSessionId) {
        this.otpRequestBody = new OtpRequestBody(otpCode, bharosaSessionId);
    }

    @JsonObject
    private static class OtpRequestBody {
        private String otpCode;
        private String bharosaSessionId;
        private String otpGeneratedCode;

        private OtpRequestBody(String otpCode, String bharosaSessionId) {
            this.otpCode = otpCode;
            this.bharosaSessionId = bharosaSessionId;
        }
    }
}
