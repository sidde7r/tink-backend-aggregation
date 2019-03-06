package se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class EeOLinkingAndLoginEntity1 {
    @JsonProperty("Respuesta")
    private EELinkingLoginAnswerEntity answer;

    @JsonProperty("codigoRetorno")
    private String returnCode;

    public EELinkingLoginAnswerEntity getAnswer() {
        return answer;
    }
}
