package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.fetcher.transactionalaccount.rpc.transactions;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ErrorResponse {
    @JsonProperty("tppMessages")
    private List<ErrorTppMessage> tppMessages = null;

    @JsonProperty("transactionStatus")
    private String transactionStatus;

    public List<ErrorTppMessage> getTppMessages() {
        return tppMessages;
    }

    public String getTransactionStatus() {
        return transactionStatus;
    }
}
