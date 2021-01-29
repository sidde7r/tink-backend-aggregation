package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.fetcher.mapper;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.fetcher.card.entities.CardTransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.fetcher.card.entities.CardTransactionsEntity;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@Slf4j
public class SparebankCardTransactionMapper {

    public Collection<Transaction> toTinkTransactions(
            CardTransactionsEntity cardTransactionsEntity) {
        return Stream.concat(mapBooked(cardTransactionsEntity), mapPending(cardTransactionsEntity))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private Stream<Optional<Transaction>> mapBooked(CardTransactionsEntity cardTransactionsEntity) {
        return cardTransactionsEntity.getBooked().stream().map(x -> toTinkTransaction(x, false));
    }

    private Stream<Optional<Transaction>> mapPending(
            CardTransactionsEntity cardTransactionsEntity) {
        return cardTransactionsEntity.getPending().stream().map(x -> toTinkTransaction(x, true));
    }

    public Optional<Transaction> toTinkTransaction(
            CardTransactionEntity cardTransactionEntity, boolean isPending) {
        Transaction transaction = null;
        try {
            transaction =
                    Transaction.builder()
                            .setPending(isPending)
                            .setAmount(cardTransactionEntity.getTransactionAmount().toAmount())
                            .setDate(
                                    ObjectUtils.firstNonNull(
                                            cardTransactionEntity.getBookingDate(),
                                            cardTransactionEntity.getTransactionDate()))
                            .setDescription(cardTransactionEntity.getTransactionDetails())
                            .build();
        } catch (RuntimeException e) {
            log.error("Failed to parse transaction, it will be skipped.", e);
        }
        return Optional.ofNullable(transaction);
    }
}
