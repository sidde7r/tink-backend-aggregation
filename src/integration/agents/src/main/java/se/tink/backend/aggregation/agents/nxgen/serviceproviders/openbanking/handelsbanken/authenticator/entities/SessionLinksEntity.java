package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SessionLinksEntity {

    @JsonProperty("cancel")
    private CancelEntity cancelEntity;

    @JsonProperty("token")
    private TokenEntity tokenEntity;

    public CancelEntity getCancelEntity() {
        return cancelEntity;
    }

    public TokenEntity getTokenEntity() {
        return tokenEntity;
    }
}
