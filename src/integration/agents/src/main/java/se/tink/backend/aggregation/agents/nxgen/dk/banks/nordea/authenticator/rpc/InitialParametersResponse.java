package se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.authenticator.entities.InitialParametersResponseEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v20.rpc.NordeaResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class InitialParametersResponse extends NordeaResponse {
    private InitialParametersResponseEntity initialParametersResponse;

    public InitialParametersResponseEntity getInitialParametersResponse() {
        return initialParametersResponse;
    }
}
