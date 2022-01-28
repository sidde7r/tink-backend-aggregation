package se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ScaEntity {

    @JsonProperty("iteraciones")
    private String iterations;

    @JsonProperty("semilla")
    private String seed;

    public String getIterations() {
        return iterations;
    }

    public String getSeed() {
        return seed;
    }
}
