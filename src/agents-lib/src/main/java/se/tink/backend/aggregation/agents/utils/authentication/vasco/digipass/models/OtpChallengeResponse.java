package se.tink.backend.aggregation.agents.utils.authentication.vasco.digipass.models;

public class OtpChallengeResponse {
    private final String otpResponse;

    public OtpChallengeResponse(String otpResponse) {
        this.otpResponse = otpResponse;
    }

    public String getOtpResponse() {
        return otpResponse;
    }
}
