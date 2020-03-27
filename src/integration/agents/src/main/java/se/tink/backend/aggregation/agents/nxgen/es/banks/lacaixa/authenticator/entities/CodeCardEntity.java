package se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CodeCardEntity {
    @JsonProperty("longitudDC")
    private int dcLength;

    @JsonProperty("tarjeta")
    private String cardId;

    @JsonProperty("clave")
    private String cardIndex;

    @JsonProperty("iteraciones")
    private int iterations;

    @JsonProperty("semilla")
    private String seed;

    public int getIterations() {
        return iterations;
    }

    public String getSeed() {
        return seed;
    }

    public String getCardId() {
        return cardId;
    }

    public String getCardIndex() {
        return cardIndex;
    }
}
