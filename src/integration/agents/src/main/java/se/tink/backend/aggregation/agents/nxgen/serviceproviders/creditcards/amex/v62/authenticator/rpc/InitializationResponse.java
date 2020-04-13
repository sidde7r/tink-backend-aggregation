package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.authenticator.entities.InitializationEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class InitializationResponse {

    private InitializationEntity initialization;

    public InitializationEntity getInitialization() {
        return initialization;
    }
}
