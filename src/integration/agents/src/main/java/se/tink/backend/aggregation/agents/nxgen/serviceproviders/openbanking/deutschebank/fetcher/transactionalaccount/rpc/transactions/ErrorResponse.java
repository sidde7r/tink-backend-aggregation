package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.fetcher.transactionalaccount.rpc.transactions;

import java.util.Collections;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ErrorResponse {
    private List<ErrorTppMessage> tppMessages;

    private String transactionStatus;

    public List<ErrorTppMessage> getTppMessages() {
        return tppMessages == null ? Collections.emptyList() : tppMessages;
    }

    public String getTransactionStatus() {
        return transactionStatus;
    }
}
