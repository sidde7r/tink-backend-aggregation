package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.authenticator.entities.oidcrequestobject;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class IdTokenClaim {

    @JsonProperty("id_token")
    private IdToken idToken;

    public IdTokenClaim(String value, Boolean essential) {

        idToken = new IdToken(value, essential);
    }
}
