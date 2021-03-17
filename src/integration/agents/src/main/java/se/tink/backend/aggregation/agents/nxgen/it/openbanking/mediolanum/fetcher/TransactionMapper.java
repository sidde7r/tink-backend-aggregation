package se.tink.backend.aggregation.agents.nxgen.it.openbanking.mediolanum.fetcher;

import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.mediolanum.fetcher.data.TransactionEntity;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@Slf4j
public class TransactionMapper {
    public Optional<Transaction> toTinkTransaction(TransactionEntity transactionEntity) {
        Transaction transaction = null;
        try {
            transaction =
                    Transaction.builder()
                            .setPending(false)
                            .setAmount(transactionEntity.getTransactionAmount().toTinkAmount())
                            .setDate(transactionEntity.getBookingDate())
                            .setDescription(
                                    transactionEntity.getRemittanceInformationUnstructured())
                            .build();
        } catch (RuntimeException e) {
            log.error("Failed to parse transaction, it will be skipped.", e);
        }
        return Optional.ofNullable(transaction);
    }
}
