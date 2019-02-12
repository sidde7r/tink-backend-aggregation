package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkortv2.fetcher.rpc;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkortv2.fetcher.entity.TransactionEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class ReservationsResponse {
    private boolean moreDataExists;
    private List<TransactionEntity> reservations;

    public List<Transaction> getTinkTransactions() {
        if (Objects.isNull(reservations)) {
            return Collections.emptyList();
        }

        return reservations.stream()
                .map(TransactionEntity::toTinkTransaction)
                .collect(Collectors.toList());
    }
}
