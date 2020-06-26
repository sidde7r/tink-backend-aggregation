package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.fetcher.transactionalaccount.entity.transactions;

import java.util.Optional;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

public class BookedTransactionEntity extends TransactionDetailsBaseEntity {

    @Override
    public Transaction toTinkTransaction() {
        return Transaction.builder()
                .setPending(false)
                .setAmount(transactionAmount.toTinkAmount())
                .setDate(Optional.ofNullable(valueDate).orElse(bookingDate))
                .setDescription(getDescription())
                .build();
    }
}
