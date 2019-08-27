package se.tink.backend.aggregation.agents.nxgen.de.openbanking.santander.fetcher.transactionalaccount.entities;

import java.util.Optional;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

public class PendingTransactionEntity extends TransactionDetailsEntity {

    @Override
    public Transaction toTinkTransaction() {
        return Transaction.builder()
                .setPending(true)
                .setAmount(transactionAmount.toAmount())
                .setDate(Optional.ofNullable(bookingDate).orElse(valueDate))
                .setDescription(remittanceInformationUnstructured)
                .build();
    }
}
