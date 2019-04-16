package se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.worklight.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public final class AuthorizationEntity {
    @JsonProperty private String wl_authenticityRealm;

    public AuthorizationEntity(final String wl_authenticityRealm) {
        this.wl_authenticityRealm = wl_authenticityRealm;
    }
}
