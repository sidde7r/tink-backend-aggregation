package se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.authenticator.entity.ActivationResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TokenActivationResponse {
    private ActivationResponse data;

    public ActivationResponse getData() {
        return data;
    }
}
