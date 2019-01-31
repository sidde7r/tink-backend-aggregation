package se.tink.backend.aggregation.agents.banks.danskebank.v2.rpc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LoginResponse extends AbstractResponse {
    @JsonProperty("Challenge")
    private String challenge;
    @JsonProperty("ChallengeData")
    private String challengeData;
    @JsonProperty("ChallengeHelp")
    private String challengeHelp;
    @JsonProperty("ChallengeNeeded")
    private boolean challengeNeeded;
    @JsonProperty("ChangePasswordRequired")
    protected boolean changePasswordRequired;
    @JsonProperty("CustomerSegment")
    protected String customerSegment;
    @JsonProperty("Name")
    protected String name;
    @JsonProperty("SecurityKey")
    private String securityKey;
    @JsonProperty("SGSegment")
    protected String sgSegment;
    @JsonProperty("UserHash")
    protected String userHash;
    @JsonProperty("SGSession")
    private String sgSession;

    public String getChallenge() {
        return challenge;
    }

    public String getChallengeData() {
        return challengeData;
    }

    public String getChallengeHelp() {
        return challengeHelp;
    }

    public String getCustomerSegment() {
        return customerSegment;
    }

    public String getName() {
        return name;
    }

    public String getSecurityKey() {
        return securityKey;
    }

    public String getSgSegment() {
        return sgSegment;
    }

    public String getUserHash() {
        return userHash;
    }

    public boolean isChallengeNeeded() {
        return challengeNeeded;
    }

    public boolean isChangePasswordRequired() {
        return changePasswordRequired;
    }

    public void setChallenge(String challenge) {
        this.challenge = challenge;
    }

    public void setChallengeData(String challengeData) {
        this.challengeData = challengeData;
    }

    public void setChallengeHelp(String challengeHelp) {
        this.challengeHelp = challengeHelp;
    }

    public void setChallengeNeeded(boolean challengeNeeded) {
        this.challengeNeeded = challengeNeeded;
    }

    public void setChangePasswordRequired(boolean changePasswordRequired) {
        this.changePasswordRequired = changePasswordRequired;
    }

    public void setCustomerSegment(String customerSegment) {
        this.customerSegment = customerSegment;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSecurityKey(String securityKey) {
        this.securityKey = securityKey;
    }

    public void setSgSegment(String sgSegment) {
        this.sgSegment = sgSegment;
    }

    public void setUserHash(String userHash) {
        this.userHash = userHash;
    }

    public String getSgSession() {
        return sgSession;
    }

    public void setSgSession(String sgSession) {
        this.sgSession = sgSession;
    }
}
