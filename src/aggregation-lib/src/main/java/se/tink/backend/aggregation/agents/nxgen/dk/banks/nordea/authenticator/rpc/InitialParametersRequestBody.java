package se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.authenticator.entities.InitialParametersRequestEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class InitialParametersRequestBody {
    private InitialParametersRequestEntity initialParametersRequest;

    public InitialParametersRequestBody setInitialParametersRequest(
            InitialParametersRequestEntity initialParametersRequest) {
        this.initialParametersRequest = initialParametersRequest;
        return this;
    }
}
