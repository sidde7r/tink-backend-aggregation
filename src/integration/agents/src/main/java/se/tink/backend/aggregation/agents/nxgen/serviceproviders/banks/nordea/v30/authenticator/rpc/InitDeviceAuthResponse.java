package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.authenticator.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class InitDeviceAuthResponse {

    private String nonce;

    public String getNonce() {
        return nonce;
    }
}
