package se.tink.backend.aggregation.agents.utils.authentication.encap.rpc;

import se.tink.backend.aggregation.agents.utils.authentication.encap.entities.AuthenticationResultEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AuthenticationResponse extends AbstractResponse {
    private AuthenticationResultEntity result;

    public AuthenticationResultEntity getResult() {
        return result;
    }
}
