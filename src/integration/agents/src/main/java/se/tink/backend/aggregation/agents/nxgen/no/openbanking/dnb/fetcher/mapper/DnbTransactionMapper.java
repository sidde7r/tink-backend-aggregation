package se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.fetcher.mapper;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.fetcher.data.entity.TransactionDetailsEntity;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.fetcher.data.entity.TransactionEntity;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@Slf4j
public class DnbTransactionMapper {

    public Collection<Transaction> toTinkTransactions(TransactionEntity transactionEntity) {
        return Stream.concat(mapBooked(transactionEntity), mapPending(transactionEntity))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private Stream<Optional<Transaction>> mapBooked(TransactionEntity transactionEntity) {
        return transactionEntity.getBooked().stream().map(x -> toTinkTransaction(x, false));
    }

    private Stream<Optional<Transaction>> mapPending(TransactionEntity transactionEntity) {
        return transactionEntity.getPending().stream().map(x -> toTinkTransaction(x, true));
    }

    public Optional<Transaction> toTinkTransaction(
            TransactionDetailsEntity transactionEntity, boolean isPending) {
        Transaction transaction = null;
        try {
            transaction =
                    Transaction.builder()
                            .setPending(isPending)
                            .setAmount(transactionEntity.getTransactionAmount().toTinkAmount())
                            .setDate(transactionEntity.getBookingDate())
                            .setDescription(
                                    ObjectUtils.firstNonNull(
                                            transactionEntity.getAdditionalInformation(),
                                            transactionEntity.getTransactionDetails()))
                            .build();
        } catch (RuntimeException e) {
            log.error("Failed to parse transaction, it will be skipped.", e);
        }
        return Optional.ofNullable(transaction);
    }
}
