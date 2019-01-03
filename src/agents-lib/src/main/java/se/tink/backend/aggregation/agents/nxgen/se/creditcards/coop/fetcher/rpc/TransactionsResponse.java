package se.tink.backend.aggregation.agents.nxgen.se.creditcards.coop.fetcher.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.se.creditcards.coop.fetcher.entities.TransactionEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;

@JsonObject
public class TransactionsResponse {

    @JsonProperty("GetTransactionsResult")
    private List<TransactionEntity> transactions;

    public PaginatorResponse getTinkTransactions(int offset, int batchSize) {
        if (offset >= transactions.size()) {
            return PaginatorResponseImpl.createEmpty(false);
        }

        List<TransactionEntity> mostRecentTransactions = transactions.subList(offset, transactions.size());

        return PaginatorResponseImpl.create(mostRecentTransactions.stream()
                .map(TransactionEntity::toTinkTransaction)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList()),
                transactions.size() >= offset + batchSize);
    }
}
