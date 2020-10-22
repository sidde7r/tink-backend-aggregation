package se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.fetcher.checking.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.fetcher.checking.entity.TransactionEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
@Getter
public class TransactionsResponse implements PaginatorResponse {
    private Body body;

    @JsonIgnore private String currency;

    public PaginatorResponse enrichWithAccountCurrency(String currency) {
        this.currency = currency;
        return this;
    }

    @Getter
    @JsonObject
    public static class Body {
        @JsonProperty("movimenti")
        private List<TransactionEntity> transactions;
    }

    @Override
    public Collection<? extends Transaction> getTinkTransactions() {
        return getBody().getTransactions().stream()
                .map(transactionEntity -> transactionEntity.toTinkTransaction(this.currency))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Boolean> canFetchMore() {
        return Optional.of(!getBody().getTransactions().isEmpty());
    }
}
