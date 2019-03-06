package se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class EeOLinkingAndLoginEntity2 {
    @JsonProperty("Respuesta")
    private EELoginAnswerEntity answer;

    @JsonProperty("codigoRetorno")
    private String returnCode;

    public EELoginAnswerEntity getAnswer() {
        return answer;
    }

    public String getReturnCode() {
        return returnCode;
    }
}
