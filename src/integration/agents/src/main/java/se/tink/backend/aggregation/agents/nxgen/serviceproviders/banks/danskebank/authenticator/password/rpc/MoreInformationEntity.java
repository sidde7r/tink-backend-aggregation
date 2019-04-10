package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.authenticator.password.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankConstants;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class MoreInformationEntity {
    @JsonProperty("FinalStatus")
    private String finalStatus;

    @JsonProperty("SessionID")
    private String sessionId;

    @JsonProperty("SessionReturnCode")
    private String sessionReturnCode;

    @JsonProperty("BusinessStatus")
    private String businessStatus;

    @JsonProperty("SecurityLevelNeeded")
    private int securityLevelNeeded;

    @JsonProperty("Reason")
    private String reason;

    @JsonProperty("OtpChallenge")
    private String otpChallenge;

    @JsonProperty("OtpMultipleDevices")
    private boolean otpMultipleDevices;

    @JsonProperty("OtpDeviceCodesLeft")
    private int otpDeviceCodesLeft;

    @JsonProperty("OtpDeviceSerialNo")
    private String otpDeviceSerialNo;

    @JsonProperty("OtpDeviceType")
    private String otpDeviceType;

    @JsonProperty("EupToken")
    private String eupToken;

    @JsonProperty("SessionCookie")
    private String sessionCookie;

    public String getFinalStatus() {
        return finalStatus;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getSessionReturnCode() {
        return sessionReturnCode;
    }

    public String getBusinessStatus() {
        return businessStatus;
    }

    public int getSecurityLevelNeeded() {
        return securityLevelNeeded;
    }

    public String getReason() {
        return reason;
    }

    public String getOtpChallenge() {
        return otpChallenge;
    }

    public boolean isOtpMultipleDevices() {
        return otpMultipleDevices;
    }

    public int getOtpDeviceCodesLeft() {
        return otpDeviceCodesLeft;
    }

    public String getOtpDeviceSerialNo() {
        return otpDeviceSerialNo;
    }

    public String getOtpDeviceType() {
        return otpDeviceType;
    }

    public String getEupToken() {
        return eupToken;
    }

    public String getSessionCookie() {
        return sessionCookie;
    }

    public boolean isChallengeInvalid() {
        return DanskeBankConstants.SecuritySystem.CHALLENGE_INVALID.equalsIgnoreCase(otpChallenge);
    }
}
