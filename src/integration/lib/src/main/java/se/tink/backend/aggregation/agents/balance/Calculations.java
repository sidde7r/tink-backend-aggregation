package se.tink.backend.aggregation.agents.balance;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.Pair;
import se.tink.backend.aggregation.agents.models.Transaction;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class Calculations {

    private static final String PENDING_TRANSACTIONS_FOUND_MSG = "Pending transactions found: ";
    private static final String BOOKED_TRANSACTIONS_FOUND_MSG = "Booked transactions found: ";
    private static final String TRANSACTIONS_SUM_MSG = "Sum of these transactions: ";
    private static final String BALANCE_SNAPSHOT_FOUND_MSG = "balance snapshot found: ";
    private static final String CALCULATION_RESULT_MSG = "Calculation result: ";
    private static final String PENDING_TRX_WITH_BOOKING_DATE_AFTER_MSG =
            "Pending transactions with booking date after ";

    public static final Calculation returnBalanceAmountAsIs =
            (balanceWithSnapshotTime, transactions) ->
                    Pair.of(
                            Optional.ofNullable(balanceWithSnapshotTime.getLeft()),
                            CalculationSummary.of("Returning balance amount as is"));

    public static final Calculation subtractPendingTransactions =
            (balanceWithSnapshotTime, transactions) -> {
                CalculationSummary calculationSummary =
                        CalculationSummary.of("Subtract pending transactions");

                ExactCurrencyAmount inputBalance = balanceWithSnapshotTime.getLeft();
                String currencyCode = inputBalance.getCurrencyCode();

                List<Transaction> pendingTransactions = getPendingTransactions(transactions);
                calculationSummary.addStepDescription(
                        PENDING_TRANSACTIONS_FOUND_MSG + pendingTransactions.size());

                ExactCurrencyAmount sum = sum(pendingTransactions, currencyCode);
                calculationSummary.addStepDescription(TRANSACTIONS_SUM_MSG + sum.getExactValue());

                ExactCurrencyAmount result = inputBalance.subtract(sum);
                calculationSummary.addStepDescription(
                        CALCULATION_RESULT_MSG + result.getExactValue());

                return Pair.of(Optional.ofNullable(result), calculationSummary);
            };

    public static final Calculation subtractPendingTransactionsWithBookingDateAfterBalanceSnapshot =
            (balanceWithSnapshotTime, transactions) -> {
                CalculationSummary calculationSummary =
                        CalculationSummary.of(
                                "Subtract pending transactions with booking date after balance snapshot");

                ExactCurrencyAmount inputBalance = balanceWithSnapshotTime.getLeft();
                String currencyCode = inputBalance.getCurrencyCode();

                List<Transaction> pendingTransactions = getPendingTransactions(transactions);
                calculationSummary.addStepDescription(
                        PENDING_TRANSACTIONS_FOUND_MSG + pendingTransactions.size());

                List<Transaction> filteredTransactions =
                        getTransactionsWithBookingDateAfterBalanceSnapshot(
                                pendingTransactions, balanceWithSnapshotTime.getRight());

                calculationSummary.addStepDescription(
                        PENDING_TRX_WITH_BOOKING_DATE_AFTER_MSG
                                + balanceWithSnapshotTime.getRight()
                                + BALANCE_SNAPSHOT_FOUND_MSG
                                + filteredTransactions.size());

                ExactCurrencyAmount sum = sum(filteredTransactions, currencyCode);
                calculationSummary.addStepDescription(TRANSACTIONS_SUM_MSG + sum.getExactValue());

                ExactCurrencyAmount result = inputBalance.subtract(sum);
                calculationSummary.addStepDescription(
                        CALCULATION_RESULT_MSG + result.getExactValue());

                return Pair.of(Optional.ofNullable(result), calculationSummary);
            };

    public static final Calculation addPendingTransactionsWithBookingDateAfterBalanceSnapshot =
            (balanceWithSnapshotTime, transactions) -> {
                CalculationSummary calculationSummary =
                        CalculationSummary.of(
                                "Add pending transactions with booking date after balance snapshot");

                ExactCurrencyAmount inputBalance = balanceWithSnapshotTime.getLeft();
                String currencyCode = inputBalance.getCurrencyCode();

                List<Transaction> pendingTransactions = getPendingTransactions(transactions);
                calculationSummary.addStepDescription(
                        PENDING_TRANSACTIONS_FOUND_MSG + pendingTransactions.size());

                List<Transaction> filteredTransactions =
                        getTransactionsWithBookingDateAfterBalanceSnapshot(
                                pendingTransactions, balanceWithSnapshotTime.getRight());
                calculationSummary.addStepDescription(
                        PENDING_TRX_WITH_BOOKING_DATE_AFTER_MSG
                                + balanceWithSnapshotTime.getRight()
                                + BALANCE_SNAPSHOT_FOUND_MSG
                                + filteredTransactions.size());

                ExactCurrencyAmount sum = sum(filteredTransactions, currencyCode);
                calculationSummary.addStepDescription(TRANSACTIONS_SUM_MSG + sum.getExactValue());

                ExactCurrencyAmount result = inputBalance.add(sum);
                calculationSummary.addStepDescription(
                        CALCULATION_RESULT_MSG + result.getExactValue());

                return Pair.of(Optional.ofNullable(result), calculationSummary);
            };

    public static final Calculation addBookedTransactionsWithBookingDateAfterBalanceSnapshot =
            (balanceWithSnapshotTime, transactions) -> {
                CalculationSummary calculationSummary =
                        CalculationSummary.of(
                                "Add booked transactions with booking date after balance snapshot");

                ExactCurrencyAmount inputBalance = balanceWithSnapshotTime.getLeft();
                String currencyCode = inputBalance.getCurrencyCode();

                List<Transaction> bookedTransactions = getBookedTransactions(transactions);
                calculationSummary.addStepDescription(
                        BOOKED_TRANSACTIONS_FOUND_MSG + bookedTransactions.size());

                List<Transaction> filteredTransactions =
                        getTransactionsWithBookingDateAfterBalanceSnapshot(
                                bookedTransactions, balanceWithSnapshotTime.getRight());
                calculationSummary.addStepDescription(
                        "Booked transactions with booking date after "
                                + balanceWithSnapshotTime.getRight()
                                + BALANCE_SNAPSHOT_FOUND_MSG
                                + filteredTransactions.size());

                ExactCurrencyAmount sum = sum(filteredTransactions, currencyCode);
                calculationSummary.addStepDescription(TRANSACTIONS_SUM_MSG + sum.getExactValue());

                ExactCurrencyAmount result = inputBalance.add(sum);
                calculationSummary.addStepDescription(
                        CALCULATION_RESULT_MSG + result.getExactValue());

                return Pair.of(Optional.ofNullable(result), calculationSummary);
            };

    public static ExactCurrencyAmount sum(List<Transaction> transactions, String currencyCode) {
        return transactions.stream()
                .map(Transaction::getAmount)
                .map(amount -> ExactCurrencyAmount.of(amount, currencyCode))
                .reduce(ExactCurrencyAmount::add)
                .orElse(ExactCurrencyAmount.zero(currencyCode));
    }

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
}
