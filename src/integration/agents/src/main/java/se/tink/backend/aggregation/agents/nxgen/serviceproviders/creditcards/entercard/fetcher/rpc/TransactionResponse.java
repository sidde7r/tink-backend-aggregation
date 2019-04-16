package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.entercard.fetcher.rpc;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@SuppressWarnings("unused")
@JsonObject
public class TransactionResponse {

    private List<Transaction> transactions = null;
    private Pagination pagination;

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public Pagination getPagination() {
        return pagination;
    }
}
