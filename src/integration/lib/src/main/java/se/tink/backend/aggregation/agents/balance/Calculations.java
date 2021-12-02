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

    private static final String INPUT_BALANCE_AMOUNT_MSG = "Input balance amount: ";
    private static final String PENDING_TRANSACTIONS_FOUND_MSG = "Pending transactions found: ";
    private static final String BOOKED_TRANSACTIONS_FOUND_MSG = "Booked transactions found: ";
    private static final String TRANSACTIONS_SUM_MSG = "^ transactions sum: ";
    private static final String BALANCE_SNAPSHOT_FOUND_MSG = "balance snapshot found: ";
    private static final String CALCULATION_RESULT_MSG = "Calculation result: ";
    private static final String PENDING_TRX_WITH_BOOKING_DATE_AFTER_MSG =
            "Pending transactions with booking date after ";

    public static final Calculation returnBalanceAmountAsIs =
            (balanceWithSnapshotTime, transactions) -> {
                log.info("[BALANCE CALCULATOR SUMMARY] Returning balance amount as is");

                return Optional.ofNullable(balanceWithSnapshotTime.getLeft());
            };

    public static final Calculation subtractPendingTransactions =
            (balanceWithSnapshotTime, transactions) -> {
                StringBuilder summary = new StringBuilder();
                summary.append("Calculation: Subtract pending transactions\n");

                ExactCurrencyAmount inputBalance = balanceWithSnapshotTime.getLeft();
                String currencyCode = inputBalance.getCurrencyCode();
                summary.append(INPUT_BALANCE_AMOUNT_MSG).append(inputBalance).append("\n");

                List<Transaction> pendingTransactions = getPendingTransactions(transactions);
                summary.append(PENDING_TRANSACTIONS_FOUND_MSG)
                        .append(pendingTransactions.size())
                        .append("\n");

                Optional<ExactCurrencyAmount> sum = sum(pendingTransactions, currencyCode);
                summary.append(TRANSACTIONS_SUM_MSG).append(sum).append("\n");

                Optional<ExactCurrencyAmount> result = sum.map(inputBalance::subtract);
                summary.append(CALCULATION_RESULT_MSG).append(result);

                logSummary(summary);
                return result;
            };

    public static final Calculation subtractPendingTransactionsWithBookingDateAfterBalanceSnapshot =
            (balanceWithSnapshotTime, transactions) -> {
                StringBuilder summary = new StringBuilder();
                summary.append(
                        "Calculation: Subtract pending transactions with booking date after balance snapshot\n");

                ExactCurrencyAmount inputBalance = balanceWithSnapshotTime.getLeft();
                String currencyCode = inputBalance.getCurrencyCode();
                summary.append(INPUT_BALANCE_AMOUNT_MSG).append(inputBalance).append("\n");

                List<Transaction> pendingTransactions = getPendingTransactions(transactions);
                summary.append(PENDING_TRANSACTIONS_FOUND_MSG)
                        .append(pendingTransactions.size())
                        .append("\n");

                List<Transaction> filteredTransactions =
                        getTransactionsWithBookingDateAfterBalanceSnapshot(
                                pendingTransactions, balanceWithSnapshotTime.getRight());
                summary.append(PENDING_TRX_WITH_BOOKING_DATE_AFTER_MSG)
                        .append(balanceWithSnapshotTime.getRight())
                        .append(BALANCE_SNAPSHOT_FOUND_MSG)
                        .append(filteredTransactions.size())
                        .append("\n");

                Optional<ExactCurrencyAmount> sum = sum(filteredTransactions, currencyCode);
                summary.append(TRANSACTIONS_SUM_MSG).append(sum).append("\n");

                Optional<ExactCurrencyAmount> result = sum.map(inputBalance::subtract);
                summary.append(CALCULATION_RESULT_MSG).append(result);

                logSummary(summary);
                return result;
            };

    public static final Calculation addPendingTransactionsWithBookingDateAfterBalanceSnapshot =
            (balanceWithSnapshotTime, transactions) -> {
                StringBuilder summary = new StringBuilder();
                summary.append(
                        "Calculation: Add pending transactions with booking date after balance snapshot\n");

                ExactCurrencyAmount inputBalance = balanceWithSnapshotTime.getLeft();
                String currencyCode = inputBalance.getCurrencyCode();
                summary.append(INPUT_BALANCE_AMOUNT_MSG).append(inputBalance).append("\n");

                List<Transaction> pendingTransactions = getPendingTransactions(transactions);
                summary.append(PENDING_TRANSACTIONS_FOUND_MSG)
                        .append(pendingTransactions.size())
                        .append("\n");

                List<Transaction> filteredTransactions =
                        getTransactionsWithBookingDateAfterBalanceSnapshot(
                                pendingTransactions, balanceWithSnapshotTime.getRight());
                summary.append(PENDING_TRX_WITH_BOOKING_DATE_AFTER_MSG)
                        .append(balanceWithSnapshotTime.getRight())
                        .append(BALANCE_SNAPSHOT_FOUND_MSG)
                        .append(filteredTransactions.size())
                        .append("\n");

                Optional<ExactCurrencyAmount> sum = sum(filteredTransactions, currencyCode);
                summary.append(TRANSACTIONS_SUM_MSG).append(sum).append("\n");

                Optional<ExactCurrencyAmount> result = sum.map(inputBalance::add);
                summary.append(CALCULATION_RESULT_MSG).append(result);

                logSummary(summary);
                return result;
            };

    public static final Calculation addBookedTransactionsWithBookingDateAfterBalanceSnapshot =
            (balanceWithSnapshotTime, transactions) -> {
                StringBuilder summary = new StringBuilder();
                summary.append(
                        "Calculation: Add booked transactions with booking date after balance snapshot\n");

                ExactCurrencyAmount inputBalance = balanceWithSnapshotTime.getLeft();
                String currencyCode = inputBalance.getCurrencyCode();
                summary.append(INPUT_BALANCE_AMOUNT_MSG).append(inputBalance).append("\n");

                List<Transaction> bookedTransactions = getBookedTransactions(transactions);
                summary.append(BOOKED_TRANSACTIONS_FOUND_MSG)
                        .append(bookedTransactions.size())
                        .append("\n");

                List<Transaction> filteredTransactions =
                        getTransactionsWithBookingDateAfterBalanceSnapshot(
                                bookedTransactions, balanceWithSnapshotTime.getRight());
                summary.append("Booked transactions with booking date after ")
                        .append(balanceWithSnapshotTime.getRight())
                        .append(BALANCE_SNAPSHOT_FOUND_MSG)
                        .append(filteredTransactions.size())
                        .append("\n");

                Optional<ExactCurrencyAmount> sum = sum(filteredTransactions, currencyCode);
                summary.append(TRANSACTIONS_SUM_MSG).append(sum).append("\n");

                Optional<ExactCurrencyAmount> result = sum.map(inputBalance::add);
                summary.append(CALCULATION_RESULT_MSG).append(result);

                logSummary(summary);
                return result;
            };

    private static List<Transaction> getPendingTransactions(List<Transaction> transactions) {
        return transactions.stream().filter(Transaction::isPending).collect(Collectors.toList());
    }

    private static List<Transaction> getBookedTransactions(List<Transaction> transactions) {
        return transactions.stream().filter(Transaction::isBooked).collect(Collectors.toList());
    }

    private static List<Transaction> getTransactionsWithBookingDateAfterBalanceSnapshot(
            List<Transaction> transactions, Instant balanceSnapshotTime) {
        return transactions.stream()
                .filter(
                        TransactionBookingDateComparator.isTransactionBookingDateAfter(
                                balanceSnapshotTime))
                .collect(Collectors.toList());
    }

    private static Optional<ExactCurrencyAmount> sum(
            List<Transaction> transactions, String currencyCode) {
        return transactions.stream()
                .map(Transaction::getAmount)
                .map(amount -> ExactCurrencyAmount.of(amount, currencyCode))
                .reduce(ExactCurrencyAmount::add);
    }

    private static void logSummary(StringBuilder msg) {
        log.info("[BALANCE CALCULATOR SUMMARY]\n\n {}", msg);
    }
}
