package se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.authenticator.entities.DomainSettings;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.authenticator.entities.OtpChallenge;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.authenticator.entities.PersonalAdvisor;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.authenticator.entities.TermsAcceptanceInfo;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.authenticator.entities.UserAccountInfo;

public class LoginDetailsResponse {

    @JsonProperty("domainSettings")
    private DomainSettings domainSettings;

    @JsonProperty("userAccountInfo")
    private UserAccountInfo userAccountInfo;

    @JsonProperty("otpChallenge")
    private OtpChallenge otpChallenge;

    @JsonProperty("termsAcceptanceInfo")
    private TermsAcceptanceInfo termsAcceptanceInfo;

    @JsonProperty("personalAdvisor")
    private PersonalAdvisor personalAdvisor;

    public UserAccountInfo getUserAccountInfo() {
        return userAccountInfo;
    }
}
