package se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.CommerzbankConstants.Values;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@AllArgsConstructor
@NoArgsConstructor
public class LoginInfoEntity {
    private String challenge;
    private String loginStatus;

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
