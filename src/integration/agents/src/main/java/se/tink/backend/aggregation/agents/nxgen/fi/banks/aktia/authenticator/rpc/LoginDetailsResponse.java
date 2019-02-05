package se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.authenticator.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LoginDetailsResponse {
    private DomainSettingsEntity domainSettings;
    private OtpChallengeEntity otpChallenge;
    private UserAccountInfoEntity userAccountInfo;
    private TermsAcceptanceInfo termsAcceptanceInfo;

    public DomainSettingsEntity getDomainSettings() {
        return domainSettings;
    }

    public OtpChallengeEntity getOtpChallenge() {
        return otpChallenge;
    }

    public UserAccountInfoEntity getUserAccountInfo() {
        return userAccountInfo;
    }

    public TermsAcceptanceInfo getTermsAcceptanceInfo() {
        return termsAcceptanceInfo;
    }
}
