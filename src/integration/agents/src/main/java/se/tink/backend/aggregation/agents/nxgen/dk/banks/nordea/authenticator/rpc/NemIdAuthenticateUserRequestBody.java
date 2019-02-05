package se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.authenticator.entities.NemidAuthenticateUserEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class NemIdAuthenticateUserRequestBody {
    NemidAuthenticateUserEntity nemIdAuthenticateUserRequest;

    public NemIdAuthenticateUserRequestBody setNemIdAuthenticateUserRequest(
            NemidAuthenticateUserEntity nemIdAuthenticateUserRequest) {
        this.nemIdAuthenticateUserRequest = nemIdAuthenticateUserRequest;
        return this;
    }
}
