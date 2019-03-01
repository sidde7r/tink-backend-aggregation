package se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class EeOLoginEntity {
    @JsonProperty("Respuesta")
    private AnswerEntity answer;

    @JsonProperty("codigoRetorno")
    private String returnCode;

    public AnswerEntity getAnswer() {
        return answer;
    }
}
