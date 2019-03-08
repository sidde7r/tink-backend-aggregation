package se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.entities.EeOBaseEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class EeOGlobalbePositionEntity extends EeOBaseEntity {
    @JsonProperty("Respuesta")
    private AnswerEntityGlobalPositionResponse answer;

    public AnswerEntityGlobalPositionResponse getAnswer() {
        return answer;
    }
}
