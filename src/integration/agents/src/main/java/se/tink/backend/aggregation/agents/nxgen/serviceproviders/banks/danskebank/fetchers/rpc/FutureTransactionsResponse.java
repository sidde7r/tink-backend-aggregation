package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.rpc.AbstractResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FutureTransactionsResponse extends AbstractResponse {
    private List<TransactionEntity> transactions;
    private String repositionKey;
    private int numberFuture;

    public List<TransactionEntity> getTransactions() {
        return transactions;
    }

    public String getRepositionKey() {
        return repositionKey;
    }

    public int getNumberFuture() {
        return numberFuture;
    }
}
