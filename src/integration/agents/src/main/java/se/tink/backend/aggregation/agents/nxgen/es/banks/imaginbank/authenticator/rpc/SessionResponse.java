package se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SessionResponse {

    @JsonProperty("iteraciones")
    private String iterations;

    @JsonProperty("semilla")
    private String seed;

    @JsonProperty("operacion")
    private String operation;

    @JsonProperty("constante")
    private String constant;

    public String getIterations() {
        return iterations;
    }

    public String getSeed() {
        return seed;
    }
}
