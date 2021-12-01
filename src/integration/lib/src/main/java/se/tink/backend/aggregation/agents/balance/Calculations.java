package se.tink.backend.aggregation.agents.balance;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.models.Transaction;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class Calculations {

    private static final Logger log = LoggerFactory.getLogger(Calculations.class);

    public static final Calculation returnBalanceAmountAsIs =
            (balanceWithSnapshotTime, transactions) -> {
                log.info("[BALANCE CALCULATOR] Returning balance amount as is");

                return Optional.ofNullable(balanceWithSnapshotTime.getLeft());
            };

    public static final Calculation subtractPendingTransactions =
            (balanceWithSnapshotTime, transactions) -> {
                log.info("[BALANCE CALCULATOR] Subtracting pending transactions");

                ExactCurrencyAmount inputBalance = balanceWithSnapshotTime.getLeft();
                String currencyCode = inputBalance.getCurrencyCode();
                List<Transaction> pendingTransactions = getPendingTransactions(transactions);

                return sum(pendingTransactions, currencyCode).map(inputBalance::subtract);
            };

    public static final Calculation subtractPendingTransactionsWithBookingDateAfterBalanceSnapshot =
            (balanceWithSnapshotTime, transactions) -> {
                log.info(
                        "[BALANCE CALCULATOR] Subtracting pending transactions that will be booked after balance snapshot");

                ExactCurrencyAmount inputBalance = balanceWithSnapshotTime.getLeft();
                String currencyCode = inputBalance.getCurrencyCode();

                List<Transaction> pendingTransactions = getPendingTransactions(transactions);
                List<Transaction> filteredTransactions =
                        getTransactionsWithBookingDateAfterBalanceSnapshot(
                                pendingTransactions, balanceWithSnapshotTime.getRight());
                return sum(filteredTransactions, currencyCode).map(inputBalance::subtract);
            };

    public static final Calculation addPendingTransactionsWithBookingDateAfterBalanceSnapshot =
            (balanceWithSnapshotTime, transactions) -> {
                log.info(
                        "[BALANCE CALCULATOR] Add pending transactions that will be booked after balance snapshot");

                ExactCurrencyAmount inputBalance = balanceWithSnapshotTime.getLeft();
                String currencyCode = inputBalance.getCurrencyCode();

                List<Transaction> pendingTransactions = getPendingTransactions(transactions);
                List<Transaction> filteredTransactions =
                        getTransactionsWithBookingDateAfterBalanceSnapshot(
                                pendingTransactions, balanceWithSnapshotTime.getRight());
                return sum(filteredTransactions, currencyCode).map(inputBalance::add);
            };

    public static final Calculation addBookedTransactionsWithBookingDateAfterBalanceSnapshot =
            (balanceWithSnapshotTime, transactions) -> {
                log.info(
                        "[BALANCE CALCULATOR] Add booked transactions that will be booked after balance snapshot");

                ExactCurrencyAmount inputBalance = balanceWithSnapshotTime.getLeft();
                String currencyCode = inputBalance.getCurrencyCode();

                List<Transaction> bookedTransactions = getBookedTransactions(transactions);
                List<Transaction> filteredTransactions =
                        getTransactionsWithBookingDateAfterBalanceSnapshot(
                                bookedTransactions, balanceWithSnapshotTime.getRight());
                return sum(filteredTransactions, currencyCode).map(inputBalance::add);
            };

    private static List<Transaction> getPendingTransactions(List<Transaction> transactions) {
        List<Transaction> pendingTransactions =
                transactions.stream().filter(Transaction::isPending).collect(Collectors.toList());
        log.info("[BALANCE CALCULATOR] Pending transactions {}", pendingTransactions.size());
        return pendingTransactions;
    }

    private static List<Transaction> getBookedTransactions(List<Transaction> transactions) {
        List<Transaction> bookedTransactions =
                transactions.stream().filter(Transaction::isBooked).collect(Collectors.toList());
        log.info("[BALANCE CALCULATOR] Booked transactions {}", bookedTransactions.size());
        return bookedTransactions;
    }

    private static List<Transaction> getTransactionsWithBookingDateAfterBalanceSnapshot(
            List<Transaction> transactions, Instant balanceSnapshotTime) {
        List<Transaction> filteredTransactions =
                transactions.stream()
                        .filter(
                                TransactionBookingDateComparator.isTransactionBookingDateAfter(
                                        balanceSnapshotTime))
                        .collect(Collectors.toList());

        log.info(
                "[BALANCE CALCULATOR] Transactions with booking date after {}: {}",
                balanceSnapshotTime,
                filteredTransactions.size());
        return filteredTransactions;
    }

    private static Optional<ExactCurrencyAmount> sum(
            List<Transaction> transactions, String currencyCode) {

        Optional<ExactCurrencyAmount> sum =
                transactions.stream()
                        .map(Transaction::getAmount)
                        .map(amount -> ExactCurrencyAmount.of(amount, currencyCode))
                        .reduce(ExactCurrencyAmount::add);
        log.info("[BALANCE CALCULATOR] Sum of transactions {}", sum);
        return sum;
    }
}
