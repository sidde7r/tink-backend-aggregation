package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.fetcher.creditcard.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.fetcher.creditcard.entity.CardTransaction;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class CardTransactionListResponse implements PaginatorResponse {
    // The number of transactions in the current result
    @JsonProperty private int size;

    // Which page the result represent
    @JsonProperty private int page;

    // Maximum number of transactions on a page
    @JsonProperty("page_size")
    private int pageSize;

    @JsonProperty private List<CardTransaction> transactions;

    public int getSize() {
        return size;
    }

    public int getPage() {
        return page;
    }

    public int getPageSize() {
        return pageSize;
    }

    @Override
    @JsonIgnore
    public Collection<? extends Transaction> getTinkTransactions() {
        return transactions.stream()
                .map(CardTransaction::toTinkTransaction)
                .collect(Collectors.toList());
    }

    @Override
    @JsonIgnore
    public Optional<Boolean> canFetchMore() {
        return Optional.of(transactions.size() >= pageSize);
    }
}
