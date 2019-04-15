package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.authenticator.password.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CodeAppEntity {
    @JsonProperty("ElevatedSecurityType")
    private String elevatedSecurityType;

    @JsonProperty("ElevatedSecurityReturnCode")
    private String elevatedSecurityReturnCode;

    @JsonProperty("OtpType")
    private String otpType;

    @JsonProperty("SerialNo")
    private String serialNo;

    @JsonProperty("ReOrder")
    private String reOrder;

    @JsonProperty("ChangeCard")
    private String changeCard;

    @JsonProperty("CodesLeft")
    private String codesLeft;

    @JsonProperty("Challenge")
    private String challenge;

    @JsonProperty("TimeStamp")
    private String timeStamp;

    @JsonProperty("Channel")
    private String channel;

    @JsonProperty("PollURL")
    private String pollURL;

    @JsonProperty("ClientPollTimeout")
    private String clientPollTimeout;

    @JsonProperty("Token")
    private String token;

    @JsonProperty("SecurityWordYN")
    private String securityWordYN;

    @JsonProperty("SecurityWord")
    private String securityWord;

    @JsonProperty("ActiveNotificationOverwrittenYN")
    private String activeNotificationOverwrittenYN;

    @JsonProperty("ChallengeLifetime")
    private String challengeLifetime;

    @JsonProperty("ChallengeExpiryTimestamp")
    private String challengeExpiryTimestamp;

    public String getPollURL() {
        return pollURL;
    }

    public String getToken() {
        return token;
    }
}
