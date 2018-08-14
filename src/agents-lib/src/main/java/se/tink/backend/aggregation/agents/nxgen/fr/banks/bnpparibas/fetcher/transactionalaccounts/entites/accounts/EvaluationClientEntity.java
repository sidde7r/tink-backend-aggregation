package se.tink.backend.aggregation.agents.nxgen.fr.banks.bnpparibas.fetcher.transactionalaccounts.entites.accounts;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class EvaluationClientEntity {
    @JsonProperty("indicateurStatutEvaluation")
    private int statusIndicatorEvaluation;

    public int getStatusIndicatorEvaluation() {
        return statusIndicatorEvaluation;
    }
}
