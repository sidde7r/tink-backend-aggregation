package se.tink.backend.aggregation.agents.utils.authentication.encap3.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class RegistrationEntity {
    @JsonProperty private String registrationId;
    @JsonProperty private int pinCodeLength;
    @JsonProperty private int pinCodeType;
    @JsonProperty private int otpLength;
    @JsonProperty private List<String> allowedAuthMethods;

    @JsonIgnore
    public String getRegistrationId() {
        return registrationId;
    }

    @JsonIgnore
    public int getPinCodeLength() {
        return pinCodeLength;
    }

    @JsonIgnore
    public int getPinCodeType() {
        return pinCodeType;
    }

    @JsonIgnore
    public int getOtpLength() {
        return otpLength;
    }

    @JsonIgnore
    public List<String> getAllowedAuthMethods() {
        return allowedAuthMethods;
    }
}
