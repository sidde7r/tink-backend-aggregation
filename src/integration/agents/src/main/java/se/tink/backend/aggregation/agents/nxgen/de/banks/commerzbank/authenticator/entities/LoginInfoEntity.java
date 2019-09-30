package se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.CommerzbankConstants.Values;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LoginInfoEntity {
    private String challenge;
    private String firstName;
    private String loginStatus;
    private String sessionToken;
    private String surname;
    private String userIdHash;
    private String userid;

    public String getChallenge() {
        return challenge;
    }

    public String getLoginStatus() {
        return loginStatus;
    }

    @JsonIgnore
    public boolean isTanRequestedStatus() {
        return Values.TAN_REQUESTED.equalsIgnoreCase(loginStatus);
    }

    @JsonIgnore
    public boolean isChallengeStatus() {
        return Values.CHALLENGE.equalsIgnoreCase(loginStatus);
    }
}
