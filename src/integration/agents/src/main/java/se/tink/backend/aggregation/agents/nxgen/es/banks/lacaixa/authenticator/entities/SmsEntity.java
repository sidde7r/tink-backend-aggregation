package se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SmsEntity {
    @JsonProperty("iteraciones")
    private int iterations;

    @JsonProperty("longitudEncriptOtp")
    private String otpLength;

    @JsonProperty("semilla")
    private String seed;

    @JsonProperty("movil")
    private String phoneNumber;

    public int getIterations() {
        return iterations;
    }

    public String getSeed() {
        return seed;
    }
}
