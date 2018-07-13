package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.rpc.AbstractResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

import java.util.List;

@JsonObject
public class ListTransactionsResponse extends AbstractResponse {
    private int totalPages;
    private int totalTransactions;
    private boolean endOfList;
    private List<TransactionEntity> transactions;
    private String repositionKey;

    public int getTotalPages() {
        return totalPages;
    }

    public int getTotalTransactions() {
        return totalTransactions;
    }

    public boolean isEndOfList() {
        return endOfList;
    }

    public List<TransactionEntity> getTransactions() {
        return transactions;
    }

    public String getRepositionKey() {
        return repositionKey;
    }
}
