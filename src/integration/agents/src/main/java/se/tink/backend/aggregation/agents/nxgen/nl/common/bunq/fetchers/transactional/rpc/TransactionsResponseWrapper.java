package se.tink.backend.aggregation.agents.nxgen.nl.common.bunq.fetchers.transactional.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class TransactionsResponseWrapper implements TransactionKeyPaginatorResponse<String> {
    @JsonProperty("Response")
    private List<TransactionWrapper> response;

    @JsonProperty("Pagination")
    private PaginationEntity pagination;

    public List<TransactionWrapper> getResponse() {
        return response;
    }

    public PaginationEntity getPagination() {
        return pagination;
    }

    @JsonIgnore
    private Optional<String> getMoreTransactions() {
        return Optional.ofNullable(pagination).map(PaginationEntity::getPreviousPage);
    }

    @JsonIgnore
    @Override
    public Collection<? extends Transaction> getTinkTransactions() {
        return response.stream()
                .map(TransactionWrapper::toTinkTransaction)
                .collect(Collectors.toList());
    }

    @JsonIgnore
    @Override
    public Optional<Boolean> canFetchMore() {
        return Optional.of(getMoreTransactions().isPresent());
    }

    @JsonIgnore
    @Override
    public String nextKey() {
        return getMoreTransactions().orElse(null);
    }
}
