package se.tink.backend.aggregation.agents.nxgen.nl.banks.bunq.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class InstallationRequest {
    @JsonProperty("client_public_key")
    private final String clientPublicKey;

    private InstallationRequest(String clientPublicKey) {
        this.clientPublicKey = clientPublicKey;
    }

    public static InstallationRequest createFromKey(String clientPublicKey) {
        return new InstallationRequest(clientPublicKey);
    }

    public String getClientPublicKey() {
        return clientPublicKey;
    }
}
