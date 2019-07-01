package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TokenEntity {

    @JsonProperty("hints")
    private HintsEntity hintsEntity;

    private String href;

    public HintsEntity getHintsEntity() {
        return hintsEntity;
    }

    public String getHref() {
        return href;
    }
}
