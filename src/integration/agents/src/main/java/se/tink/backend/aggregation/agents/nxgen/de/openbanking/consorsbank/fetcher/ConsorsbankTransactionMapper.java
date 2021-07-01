package se.tink.backend.aggregation.agents.nxgen.de.openbanking.consorsbank.fetcher;

import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.utils.berlingroup.fetcher.entities.TransactionEntity;
import se.tink.backend.aggregation.agents.utils.berlingroup.fetcher.mappers.TransactionMapper;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@Slf4j
public class ConsorsbankTransactionMapper implements TransactionMapper {
    public Optional<AggregationTransaction> toTinkTransaction(
            TransactionEntity transactionEntity, boolean isPending) {
        Transaction transaction = null;
        try {
            transaction =
                    Transaction.builder()
                            .setPending(isPending)
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
