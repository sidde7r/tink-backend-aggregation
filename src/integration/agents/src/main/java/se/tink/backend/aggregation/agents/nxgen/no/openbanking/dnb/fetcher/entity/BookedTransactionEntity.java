package se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.fetcher.entity;

import org.apache.commons.lang3.ObjectUtils;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

public class BookedTransactionEntity extends TransactionDetailsEntity {

    @Override
    public Transaction toTinkTransaction() {
        return Transaction.builder()
                .setPending(false)
                .setAmount(transactionAmount.toAmount())
                .setDate(bookingDate)
                .setDescription(ObjectUtils.firstNonNull(additionalInformation, transactionDetails))
                .build();
    }
}
