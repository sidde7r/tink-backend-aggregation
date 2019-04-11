package se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.entities.EeOBaseEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.fetcher.entities.AnswerEntityGlobalPositionResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class EeOFirstTimeGlobalPositionEntity extends EeOBaseEntity {
    @JsonProperty("Respuesta")
    private AnswerEntityGlobalPositionResponse answer;

    public AnswerEntityGlobalPositionResponse getAnswer() {
        return answer;
    }
}
