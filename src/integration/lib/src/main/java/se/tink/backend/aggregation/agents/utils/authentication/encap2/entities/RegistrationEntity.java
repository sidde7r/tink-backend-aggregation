package se.tink.backend.aggregation.agents.utils.authentication.encap2.entities;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class RegistrationEntity {
    private String registrationId;
    private int pinCodeLength;
    private int pinCodeType;
    private int otpLength;
    private List<String> allowedAuthMethods;

    public String getRegistrationId() {
        return registrationId;
    }

    public int getPinCodeLength() {
        return pinCodeLength;
    }

    public int getPinCodeType() {
        return pinCodeType;
    }

    public int getOtpLength() {
        return otpLength;
    }

    public List<String> getAllowedAuthMethods() {
        return allowedAuthMethods;
    }
}
