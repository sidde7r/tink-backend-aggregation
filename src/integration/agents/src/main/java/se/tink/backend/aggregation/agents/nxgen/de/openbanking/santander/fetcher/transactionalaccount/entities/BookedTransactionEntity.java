package se.tink.backend.aggregation.agents.nxgen.de.openbanking.santander.fetcher.transactionalaccount.entities;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.entities.TransactionDetailsBaseEntity;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

public class BookedTransactionEntity extends TransactionDetailsBaseEntity {

    @Override
    public Transaction toTinkTransaction() {
        return Transaction.builder()
                .setPending(false)
                .setAmount(transactionAmount.toAmount())
                .setDate(bookingDate)
                .setDescription(getTransactionDescription())
                .build();
    }
}
