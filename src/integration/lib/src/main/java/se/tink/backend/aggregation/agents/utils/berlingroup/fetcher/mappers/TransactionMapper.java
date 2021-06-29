package se.tink.backend.aggregation.agents.utils.berlingroup.fetcher.mappers;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import se.tink.backend.aggregation.agents.utils.berlingroup.fetcher.entities.TransactionEntity;
import se.tink.backend.aggregation.agents.utils.berlingroup.fetcher.entities.TransactionsEntity;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;

public interface TransactionMapper {

    default List<AggregationTransaction> toTinkTransactions(TransactionsEntity transactionsEntity) {
        if (transactionsEntity == null) {
            return Collections.emptyList();
        }
        return Stream.concat(
                        transactionsEntity.getBooked().stream()
                                .map(x -> toTinkTransaction(x, false)),
                        transactionsEntity.getPending().stream()
                                .map(x -> toTinkTransaction(x, true)))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    Optional<AggregationTransaction> toTinkTransaction(
            TransactionEntity transactionEntity, boolean isPending);
}
