package se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.fetcher.account.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.fetcher.account.entities.TransactionResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ConsultTransactionResponse {

    @JsonProperty("ConsultTransactionResponse")
    public TransactionResponse transactionResponse;

    public TransactionResponse getTransactionResponse() {
        return transactionResponse;
    }
}
