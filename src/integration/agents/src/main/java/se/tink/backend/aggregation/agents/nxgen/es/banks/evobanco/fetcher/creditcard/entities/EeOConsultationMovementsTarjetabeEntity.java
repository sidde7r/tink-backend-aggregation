package se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.fetcher.creditcard.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class EeOConsultationMovementsTarjetabeEntity {
    @JsonProperty("Respuesta")
    private AnswerEntity answer;

    @JsonProperty("codigoRetorno")
    private String returnCode;

    public String getReturnCode() {
        return returnCode;
    }

    public AnswerEntity getAnswer() {
        return answer;
    }
}
