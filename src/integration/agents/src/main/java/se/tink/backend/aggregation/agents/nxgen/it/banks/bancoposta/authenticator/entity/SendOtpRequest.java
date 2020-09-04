package se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class SendOtpRequest extends BaseRequest {
    private final Body body;

    @AllArgsConstructor
    public static class Body {
        @JsonProperty("smsOTP")
        private String smsOtp;
    }

    public SendOtpRequest(String otp) {
        this.body = new Body(otp);
    }
}
