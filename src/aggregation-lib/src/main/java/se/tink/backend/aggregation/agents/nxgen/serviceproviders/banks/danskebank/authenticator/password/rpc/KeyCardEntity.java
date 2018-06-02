package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.authenticator.password.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class KeyCardEntity {
    @JsonProperty("ElevatedSecurityType")
    private String elevatedSecurityType;
    @JsonProperty("ElevatedSecurityReturnCode")
    private String elevatedSecurityReturnCode;
    @JsonProperty("OtpType")
    private String otpType;
    @JsonProperty("SerialNo")
    private String serialNumber;
    @JsonProperty("ReOrder")
    private String reOrder;
    @JsonProperty("ChangeCard")
    private String changeCard;
    @JsonProperty("CodesLeft")
    private String codesLeft;
    @JsonProperty("Challenge")
    private String challenge;
    @JsonProperty("TimeStamp")
    private String timestamp;
    @JsonProperty("Channel")
    private String channel;

    public String getElevatedSecurityType() {
        return elevatedSecurityType;
    }

    public String getElevatedSecurityReturnCode() {
        return elevatedSecurityReturnCode;
    }

    public String getOtpType() {
        return otpType;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public String getReOrder() {
        return reOrder;
    }

    public String getChangeCard() {
        return changeCard;
    }

    public String getCodesLeft() {
        return codesLeft;
    }

    public String getChallenge() {
        return challenge;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getChannel() {
        return channel;
    }
}
