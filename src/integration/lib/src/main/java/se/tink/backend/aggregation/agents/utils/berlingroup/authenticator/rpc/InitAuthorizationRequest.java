package se.tink.backend.aggregation.agents.utils.berlingroup.authenticator.rpc;

import se.tink.backend.aggregation.agents.utils.berlingroup.consent.PsuDataEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class InitAuthorizationRequest {

    private PsuDataEntity psuData;

    public InitAuthorizationRequest(PsuDataEntity psuData) {
        this.psuData = psuData;
    }
}
