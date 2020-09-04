package se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class RegisterCodeRequest extends BaseRequest {
    private final Body body;

    @AllArgsConstructor
    public static class Body {
        @JsonProperty("smsOTP")
        private String smsOtp;

        @JsonProperty("codiceDigital")
        private String accountAlias;

        @JsonProperty("aliasWallet")
        private String walletName;
    }

    public RegisterCodeRequest(String otp, String accountAlias, String walletName) {
        this.body = new Body(otp, accountAlias, walletName);
    }
}
