package se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.authenticator.embeddedotp;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class EmbeddedRequest {

    private String username;
    private String password;
    private String otp;

    public EmbeddedRequest(String username, String password, String otp) {
        this.username = username;
        this.password = password;
        this.otp = otp;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getOtp() {
        return otp;
    }
}
