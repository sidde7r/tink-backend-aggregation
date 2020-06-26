package se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.fetcher.transactionalaccount.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.SebApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.fetcher.transactionalaccount.entities.TransactionPaginationLinksEntity;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.fetcher.transactionalaccount.entities.TransactionsEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.core.transaction.UpcomingTransaction;

@JsonObject
public class FetchTransactionsResponse {
    private String iban;

    private String bban;

    private TransactionsEntity transactions;

    @JsonProperty("_links")
    private TransactionPaginationLinksEntity links;

    public TransactionPaginationLinksEntity getLinks() {
        return links;
    }

    @JsonIgnore
    public List<Transaction> getTinkTransactions(SebApiClient apiClient) {
        return transactions.getTransactions(apiClient);
    }

    @JsonIgnore
    public List<UpcomingTransaction> getUpcomingTransactions() {
        return transactions.getUpcomingTransactions();
    }
}
