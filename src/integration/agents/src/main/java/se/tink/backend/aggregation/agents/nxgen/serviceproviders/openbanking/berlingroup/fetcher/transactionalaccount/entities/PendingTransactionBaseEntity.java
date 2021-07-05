package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.entities;

import java.util.Optional;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

public class PendingTransactionBaseEntity extends TransactionDetailsBaseEntity {

    @Override
    public Transaction toTinkTransaction() {
        return Transaction.builder()
                .setPending(true)
                .setAmount(transactionAmount.toAmount())
                .setDate(Optional.ofNullable(bookingDate).orElse(valueDate))
                .setDescription(getTransactionDescription())
                .build();
    }
}
