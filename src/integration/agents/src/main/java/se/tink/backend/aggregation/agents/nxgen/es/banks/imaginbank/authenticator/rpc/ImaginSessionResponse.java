package se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.authenticator.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ImaginSessionResponse {
    private String iterations;
    private String seed;
    private String operation;
    private String constant;

    public String getIterations() {
        return iterations;
    }

    public String getSeed() {
        return seed;
    }
}
