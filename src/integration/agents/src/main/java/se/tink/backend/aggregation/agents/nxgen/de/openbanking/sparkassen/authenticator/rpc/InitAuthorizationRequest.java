package se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.entities.PsuDataEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class InitAuthorizationRequest {

    private PsuDataEntity psuData;

    public InitAuthorizationRequest(PsuDataEntity psuData) {
        this.psuData = psuData;
    }
}
