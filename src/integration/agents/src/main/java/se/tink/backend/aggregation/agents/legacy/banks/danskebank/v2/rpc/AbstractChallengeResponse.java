package se.tink.backend.aggregation.agents.banks.danskebank.v2.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public abstract class AbstractChallengeResponse extends AbstractResponse {

    // Depending on the risk, Danske respond differently, one for a ChallengeResponse signing, the
    // other for a Confirmation.

    // Common field
    @JsonProperty("ChallengeData")
    private String challengeData;

    // For ChallengeResponse Signing
    @JsonProperty("BankID")
    private boolean bankId;

    @JsonProperty("Challenge")
    private String challenge;

    @JsonProperty("ChallengeNeeded")
    private boolean challengeNeeded;

    @JsonProperty("SecurityCardNumber")
    private String securityCardNumber;

    // For Confirmation
    @JsonProperty("ConfirmationNeeded")
    private boolean confirmationNeeded;

    public boolean isConfirmationNeeded() {
        return confirmationNeeded;
    }

    public void setConfirmationNeeded(boolean confirmationNeeded) {
        this.confirmationNeeded = confirmationNeeded;
    }

    public boolean isBankId() {
        return bankId;
    }

    public void setBankId(boolean bankId) {
        this.bankId = bankId;
    }

    public String getChallenge() {
        return challenge;
    }

    public void setChallenge(String challenge) {
        this.challenge = challenge;
    }

    public String getChallengeData() {
        return challengeData;
    }

    public void setChallengeData(String challengeData) {
        this.challengeData = challengeData;
    }

    public boolean isChallengeNeeded() {
        return challengeNeeded;
    }

    public void setChallengeNeeded(boolean challengeNeeded) {
        this.challengeNeeded = challengeNeeded;
    }

    public String getSecurityCardNumber() {
        return securityCardNumber;
    }

    public void setSecurityCardNumber(String securityCardNumber) {
        this.securityCardNumber = securityCardNumber;
    }
}
