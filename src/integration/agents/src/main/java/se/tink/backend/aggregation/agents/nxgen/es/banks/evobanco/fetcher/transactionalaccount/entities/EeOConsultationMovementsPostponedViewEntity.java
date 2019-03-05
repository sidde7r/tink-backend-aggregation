package se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class EeOConsultationMovementsPostponedViewEntity {
    @JsonProperty("Respuesta")
    private AnswerEntityTransactionsResponse answer;

    @JsonProperty("codigoRetorno")
    private String returnCode;

    @JsonProperty("Errores")
    private ErrorsEntity errors;

    public AnswerEntityTransactionsResponse getAnswer() {
        return answer;
    }

    public String getReturnCode() {
        return returnCode;
    }

    public ErrorsEntity getErrors() {
        return errors;
    }
}
