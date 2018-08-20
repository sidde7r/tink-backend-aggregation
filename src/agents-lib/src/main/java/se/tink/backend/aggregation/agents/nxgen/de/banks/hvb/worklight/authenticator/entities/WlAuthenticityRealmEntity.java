package se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.worklight.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public final class WlAuthenticityRealmEntity {
    @JsonProperty("WL-Challenge-Data")
    private String wLChallengeData;

    public String getwLChallengeData() {
        return wLChallengeData;
    }
}
