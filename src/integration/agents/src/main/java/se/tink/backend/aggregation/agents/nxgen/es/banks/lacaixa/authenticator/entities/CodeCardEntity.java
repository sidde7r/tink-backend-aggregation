package se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
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
}
