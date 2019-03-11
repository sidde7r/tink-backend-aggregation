package se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ErrorsEntity {
    @JsonProperty("solucion")
    private String solution;

    @JsonProperty("codigoMostrar")
    private String showCode;

    @JsonProperty("mensajeMostrar")
    private String messageShow;

    public String getShowCode() {
        return showCode;
    }

    @Override
    public String toString() {
        return "'solution' : "
                + solution
                + " 'showCode' : "
                + showCode
                + " 'messageShow' : "
                + messageShow;
    }
}
