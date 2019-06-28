package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

import java.util.List;

@JsonObject
public class Links {

    @JsonProperty("authorization")
    private List<AuthorizationItem> authorization;

    public List<AuthorizationItem> getAuthorization() {
        return authorization;
    }
}
