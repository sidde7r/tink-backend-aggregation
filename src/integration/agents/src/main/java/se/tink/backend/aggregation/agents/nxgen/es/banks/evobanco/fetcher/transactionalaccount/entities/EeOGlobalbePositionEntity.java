package se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class EeOGlobalbePositionEntity {
    @JsonProperty("Respuesta")
    private AnswerEntityGlobalPositionResponse answer;

    @JsonProperty("codigoRetorno")
    private String returnCode;

    public AnswerEntityGlobalPositionResponse getAnswer() {
        return answer;
    }
}
