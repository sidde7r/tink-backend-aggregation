package se.tink.backend.aggregation.agents.nxgen.se.openbanking.seb.fetcher.transactionalaccount.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collection;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.seb.fetcher.transactionalaccount.entities.LinksEntity;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.seb.fetcher.transactionalaccount.entities.TransactionsEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class FetchTransactionsResponse implements PaginatorResponse {
    @JsonProperty
    private String iban;

    @JsonProperty
    private String bban;

    @JsonProperty
    private TransactionsEntity transactions;

    @JsonProperty("_links")
    private LinksEntity links;

    @Override
    public Collection<? extends Transaction> getTinkTransactions() {
        return transactions.getTransactions();
    }

    @Override
    public Optional<Boolean> canFetchMore() {
        return Optional.of(links.hasMore());
    }
}
