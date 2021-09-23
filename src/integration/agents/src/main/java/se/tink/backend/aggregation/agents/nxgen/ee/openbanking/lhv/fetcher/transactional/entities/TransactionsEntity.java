package se.tink.backend.aggregation.agents.nxgen.ee.openbanking.lhv.fetcher.transactional.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
@Getter
public class TransactionsEntity {
    @JsonProperty("booked")
    private List<BookedTransactionEntity> bookedTransaction;

    @JsonIgnore
    public List<Transaction> getTransactions() {
        final Stream<Transaction> bookedTransactionStream =
                Optional.of(bookedTransaction).orElse(Collections.emptyList()).stream()
                        .map(BookedTransactionEntity::toTinkTransaction);

        return bookedTransactionStream.collect(Collectors.toList());
    }
}
