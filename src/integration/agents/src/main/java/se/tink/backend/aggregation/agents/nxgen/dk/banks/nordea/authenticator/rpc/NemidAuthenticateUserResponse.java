package se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.authenticator.entities.AuthenticatedUserEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v20.rpc.NordeaResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class NemidAuthenticateUserResponse extends NordeaResponse {
    AuthenticatedUserEntity authenticateUserResponse;

    public AuthenticatedUserEntity getAuthenticateUserResponse() {
        return authenticateUserResponse;
    }
}
