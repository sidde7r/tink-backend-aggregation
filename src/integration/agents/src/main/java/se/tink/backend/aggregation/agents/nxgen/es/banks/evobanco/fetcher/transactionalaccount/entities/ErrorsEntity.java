package se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.fetcher.transactionalaccount.entities;

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
}
