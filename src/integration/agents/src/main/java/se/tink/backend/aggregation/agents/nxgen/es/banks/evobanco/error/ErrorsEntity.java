package se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.error;

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

    public ErrorsEntity() {}

    public ErrorsEntity(String solution, String showCode, String messageShow) {
        this.solution = solution;
        this.showCode = showCode;
        this.messageShow = messageShow;
    }

    public String getShowCode() {
        return showCode;
    }

    public String getMessageShow() {
        return messageShow;
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
