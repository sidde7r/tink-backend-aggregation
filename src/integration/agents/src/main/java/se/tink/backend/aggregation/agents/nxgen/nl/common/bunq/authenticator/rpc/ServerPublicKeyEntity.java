package se.tink.backend.aggregation.agents.nxgen.nl.common.bunq.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ServerPublicKeyEntity {
    @JsonProperty("server_public_key")
    private String serverPublicKey;
}
