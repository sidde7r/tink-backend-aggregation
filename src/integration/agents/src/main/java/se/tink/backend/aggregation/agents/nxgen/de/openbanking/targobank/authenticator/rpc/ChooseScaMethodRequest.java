package se.tink.backend.aggregation.agents.nxgen.de.openbanking.targobank.authenticator.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ChooseScaMethodRequest {
    private String authenticationMethodId;

    public ChooseScaMethodRequest(String authenticationMethodId) {
        this.authenticationMethodId = authenticationMethodId;
    }
}
