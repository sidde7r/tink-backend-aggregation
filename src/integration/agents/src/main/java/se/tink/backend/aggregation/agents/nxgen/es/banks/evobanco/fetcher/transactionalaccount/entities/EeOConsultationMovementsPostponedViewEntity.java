package se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.entities.EeOBaseEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class EeOConsultationMovementsPostponedViewEntity extends EeOBaseEntity {
    @JsonProperty("Respuesta")
    private AnswerEntityTransactionsResponse answer;

    public AnswerEntityTransactionsResponse getAnswer() {
        return answer;
    }
}
