package se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.fetcher.creditcard.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.fetcher.TransactionsRequestBody;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FindMovementsRequest {

    public FindMovementsRequest(TransactionsRequestBody requestBody) {
        this.requestBody = requestBody;
    }

    @JsonProperty("FindMovementsRequest")
    public TransactionsRequestBody requestBody;
}
