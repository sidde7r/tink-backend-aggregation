package se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.fetcher.entity;

import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

public class PendingTransactionEntity extends TransactionDetailsEntity {

    @Override
    public Transaction toTinkTransaction() {
        return Transaction.builder()
                .setPending(true)
                .setAmount(transactionAmount.toAmount())
                .setDate(bookingDate)
                .setDescription(additionalInformation)
                .build();
    }
}
