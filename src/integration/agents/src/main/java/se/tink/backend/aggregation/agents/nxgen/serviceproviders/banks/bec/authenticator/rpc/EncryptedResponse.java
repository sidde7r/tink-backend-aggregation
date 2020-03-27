package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.authenticator.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class EncryptedResponse {
    private String encryptedPayload;

    public String getEncryptedPayload() {
        return encryptedPayload;
    }
}
