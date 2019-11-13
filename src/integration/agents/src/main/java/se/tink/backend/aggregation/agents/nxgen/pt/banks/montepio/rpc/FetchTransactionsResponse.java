package se.tink.backend.aggregation.agents.nxgen.pt.banks.montepio.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.montepio.entities.TransactionsResultEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FetchTransactionsResponse extends GenericResponse {

    @JsonProperty("Result")
    private TransactionsResultEntity resultEntity;

    public TransactionsResultEntity getResultEntity() {
        return resultEntity;
    }
}
