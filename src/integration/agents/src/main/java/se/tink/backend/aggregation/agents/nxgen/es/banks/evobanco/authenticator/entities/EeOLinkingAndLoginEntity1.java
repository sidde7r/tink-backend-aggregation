package se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.entities.EeOBaseEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class EeOLinkingAndLoginEntity1 extends EeOBaseEntity {
    @JsonProperty("Respuesta")
    private EELinkingLoginAnswerEntity answer;

    public EELinkingLoginAnswerEntity getAnswer() {
        return answer;
    }
}
