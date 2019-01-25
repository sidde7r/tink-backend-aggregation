package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class KdkEntity {
    @JsonProperty("MustUpdate")
    private boolean mustUpdate;
    @JsonProperty("PostponesLeft")
    private int postponesLeft;

    public boolean isMustUpdate() {
        return mustUpdate;
    }

    public int getPostponesLeft() {
        return postponesLeft;
    }
}
