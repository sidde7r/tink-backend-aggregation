package se.tink.backend.aggregation.agents.nxgen.se.brokers.nordnet.authenticator.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ArtifactRequest {

    private String artifact;

    public ArtifactRequest(String artifact) {
        this.artifact = artifact;
    }
}
