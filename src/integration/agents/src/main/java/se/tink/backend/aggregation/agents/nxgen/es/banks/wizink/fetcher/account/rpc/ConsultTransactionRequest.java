package se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.fetcher.account.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.fetcher.TransactionsRequestBody;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ConsultTransactionRequest {

    public ConsultTransactionRequest(TransactionsRequestBody requestBody) {
        this.requestBody = requestBody;
    }

    @JsonProperty("ConsultTransactionRequest")
    private TransactionsRequestBody requestBody;
}
