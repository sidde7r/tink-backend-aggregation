package se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.fetcher.creditcard.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.fetcher.creditcard.entities.Movements;
import se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.rpc.Result;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FindMovementsResponse {

    @JsonProperty("FindMovementsResponse")
    private Movements movements;

    private Result result;

    public Movements getMovements() {
        return movements;
    }

    public Result getResult() {
        return result;
    }
}
