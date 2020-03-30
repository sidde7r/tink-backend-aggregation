package se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class Pin1ScaEntity {
    @JsonProperty("iteraciones")
    private int iterations;

    @JsonProperty("longitudDC")
    private int dcLength;

    @JsonProperty("semilla")
    private String seed;

    public int getIterations() {
        return iterations;
    }

    public String getSeed() {
        return seed;
    }
}
