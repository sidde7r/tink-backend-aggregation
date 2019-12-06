package se.tink.backend.aggregation.agents.nxgen.de.openbanking.targobank.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.de.openbanking.targobank.authenticator.entities.PsuDataEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PasswordAuthenticationRequest {
    private PsuDataEntity psuData;

    public PasswordAuthenticationRequest(PsuDataEntity psuData) {
        this.psuData = psuData;
    }
}
