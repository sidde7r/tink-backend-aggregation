package se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class RegistrationInitResponse extends TokenResponse {
    // These are populated from http headers
    @JsonIgnore
    private String loginStatus;
    @JsonIgnore
    private String otpIndex;
    @JsonIgnore
    private String otpCard;

    public String getLoginStatus() {
        return loginStatus;
    }

    public void setLoginStatus(String loginStatus) {
        this.loginStatus = loginStatus;
    }

    public String getOtpIndex() {
        return otpIndex;
    }

    public void setOtpIndex(String otpIndex) {
        this.otpIndex = otpIndex;
    }

    public String getOtpCard() {
        return otpCard;
    }

    public void setOtpCard(String otpCard) {
        this.otpCard = otpCard;
    }
}
