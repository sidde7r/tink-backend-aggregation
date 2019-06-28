package se.tink.backend.aggregation.agents.nxgen.se.openbanking.seb.fetcher.creditcardaccount.entities;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class TransactionsEntity {

    List<CreditCardBookedEntity> booked;
    List<CreditCardPendingEntity> pending;

    public Collection<? extends Transaction> getTransactions() {
        return Stream.concat(
                        booked != null
                                ? booked.stream().map(CreditCardBookedEntity::toTinkTransaction)
                                : Stream.empty(),
                        pending != null
                                ? pending.stream().map(CreditCardPendingEntity::toTinkTransaction)
                                : Stream.empty())
                .collect(Collectors.toList());
    }
}
