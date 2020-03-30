package se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SessionResponse {

    @JsonProperty("iteraciones")
    private int iterations;

    @JsonProperty("semilla")
    private String seed;

    @JsonProperty("operacion")
    private String operation;

    @JsonProperty("constante")
    private String constant;

    public int getIterations() {
        return iterations;
    }

    public String getSeed() {
        return seed;
    }
}
