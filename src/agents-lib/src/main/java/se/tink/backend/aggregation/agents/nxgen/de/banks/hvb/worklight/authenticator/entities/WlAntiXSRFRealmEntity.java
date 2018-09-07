package se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.worklight.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public final class WlAntiXSRFRealmEntity {
    @JsonProperty("WL-Instance-Id")
    private String wLInstanceId;

    public String getwLInstanceId() {
        return wLInstanceId;
    }
}
