package se.tink.backend.aggregation.agents.summary.refresh.transactions;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import se.tink.backend.aggregation.agents.models.Transaction;
import se.tink.backend.aggregation.agents.models.TransactionDateType;

public class OldestTrxDateProvider {

    private OldestTrxDateProvider() {}

    public static Optional<LocalDate> getDate(List<Transaction> accountTransactions) {
        Set<LocalDate> dates = new HashSet<>();

        addOldestByTransactionDateProperty(accountTransactions, dates);
        addOldestByDateProperty(accountTransactions, dates);
        addOldestByTimestampProperty(accountTransactions, dates);

        return dates.stream().min(LocalDate::compareTo);
    }

    private static void addOldestByTimestampProperty(
            List<Transaction> accountTransactions, Set<LocalDate> dates) {
        getOldestTrxByCriteria(
                        accountTransactions,
                        transaction -> {
                            long timestamp = transaction.getTimestamp();
                            return timestamp == 0L ? Optional.empty() : Optional.of(timestamp);
                        })
                .map(Transaction::getTimestamp)
                .map(LocalDate::ofEpochDay)
                .ifPresent(dates::add);
    }

    private static void addOldestByDateProperty(
            List<Transaction> accountTransactions, Set<LocalDate> dates) {
        getOldestTrxByCriteria(
                        accountTransactions,
                        transaction -> Optional.ofNullable(transaction.getDate()))
                .map(Transaction::getDate)
                .map(date -> date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate())
                .ifPresent(dates::add);
    }

    private static void addOldestByTransactionDateProperty(
            List<Transaction> accountTransactions, Set<LocalDate> dates) {
        Arrays.asList(TransactionDateType.values())
                .forEach(
                        transactionDateType ->
                                getOldestTrxByCriteria(
                                                accountTransactions,
                                                trx ->
                                                        trx.getDateForTransactionDateType(
                                                                (TransactionDateType)
                                                                        transactionDateType))
                                        .flatMap(
                                                trx ->
                                                        trx.getDateForTransactionDateType(
                                                                (TransactionDateType)
                                                                        transactionDateType))
                                        .ifPresent(dates::add));
    }

    private static <T extends Comparable> Optional<Transaction> getOldestTrxByCriteria(
            List<Transaction> transactions, Function<Transaction, Optional<T>> fieldValueGetter) {
        return transactions.stream()
                .filter(t -> fieldValueGetter.apply(t).isPresent())
                .min(
                        (t1, t2) -> {
                            T date1 =
                                    fieldValueGetter
                                            .apply(t1)
                                            .orElseThrow(
                                                    () ->
                                                            new IllegalStateException(
                                                                    "Field value getter failed"));
                            T date2 =
                                    fieldValueGetter
                                            .apply(t2)
                                            .orElseThrow(
                                                    () ->
                                                            new IllegalStateException(
                                                                    "Field value getter failed"));
                            return date1.compareTo(date2);
                        });
    }
}
