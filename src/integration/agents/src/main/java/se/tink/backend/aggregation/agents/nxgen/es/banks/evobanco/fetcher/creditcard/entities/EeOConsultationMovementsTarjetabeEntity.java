package se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.fetcher.creditcard.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.entities.EeOBaseEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class EeOConsultationMovementsTarjetabeEntity extends EeOBaseEntity {
    @JsonProperty("Respuesta")
    private AnswerEntity answer;

    public Optional<AnswerEntity> getAnswer() {
        return Optional.ofNullable(answer);
    }
}
