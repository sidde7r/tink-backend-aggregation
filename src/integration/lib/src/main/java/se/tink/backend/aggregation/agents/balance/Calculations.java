package se.tink.backend.aggregation.agents.balance;

import java.util.Optional;
import se.tink.backend.aggregation.agents.models.Transaction;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class Calculations {

    public static final Calculation returnBalanceAmountAsIs =
            (balanceWithSnapshotTime, transactions) ->
                    Optional.ofNullable(balanceWithSnapshotTime.getLeft());

    public static final Calculation subtractPendingTransactions =
            (balanceWithSnapshotTime, transactions) ->
                    transactions.stream()
                            .filter(Transaction::isPending)
                            .map(Transaction::getAmount)
                            .map(
                                    amount ->
                                            ExactCurrencyAmount.of(
                                                    amount,
                                                    balanceWithSnapshotTime
                                                            .getLeft()
                                                            .getCurrencyCode()))
                            .reduce(ExactCurrencyAmount::add)
                            .map(
                                    pendingTransactionsSum ->
                                            balanceWithSnapshotTime
                                                    .getLeft()
                                                    .subtract(pendingTransactionsSum));

    public static final Calculation subtractPendingTransactionsWithBookingDateAfterBalanceSnapshot =
            (balanceWithSnapshotTime, transactions) ->
                    transactions.stream()
                            .filter(Transaction::isPending)
                            .filter(
                                    TransactionBookingDateComparator.isTransactionBookingDateAfter(
                                            balanceWithSnapshotTime.getRight()))
                            .map(Transaction::getAmount)
                            .map(
                                    amount ->
                                            ExactCurrencyAmount.of(
                                                    amount,
                                                    balanceWithSnapshotTime
                                                            .getLeft()
                                                            .getCurrencyCode()))
                            .reduce(ExactCurrencyAmount::add)
                            .map(
                                    pendingTransactionSum ->
                                            balanceWithSnapshotTime
                                                    .getLeft()
                                                    .subtract(pendingTransactionSum));

    public static final Calculation addPendingTransactionsWithBookingDateAfterBalanceSnapshot =
            (balanceWithSnapshotTime, transactions) ->
                    transactions.stream()
                            .filter(Transaction::isPending)
                            .filter(
                                    TransactionBookingDateComparator.isTransactionBookingDateAfter(
                                            balanceWithSnapshotTime.getRight()))
                            .map(Transaction::getAmount)
                            .map(
                                    amount ->
                                            ExactCurrencyAmount.of(
                                                    amount,
                                                    balanceWithSnapshotTime
                                                            .getLeft()
                                                            .getCurrencyCode()))
                            .reduce(ExactCurrencyAmount::add)
                            .map(
                                    pendingTransactionSum ->
                                            balanceWithSnapshotTime
                                                    .getLeft()
                                                    .add(pendingTransactionSum));

    public static final Calculation addBookedTransactionsWithBookingDateAfterBalanceSnapshot =
            (balanceWithSnapshotTime, transactions) ->
                    transactions.stream()
                            .filter(Transaction::isBooked)
                            .filter(
                                    TransactionBookingDateComparator.isTransactionBookingDateAfter(
                                            balanceWithSnapshotTime.getRight()))
                            .map(Transaction::getAmount)
                            .map(
                                    amount ->
                                            ExactCurrencyAmount.of(
                                                    amount,
                                                    balanceWithSnapshotTime
                                                            .getLeft()
                                                            .getCurrencyCode()))
                            .reduce(ExactCurrencyAmount::add)
                            .map(
                                    bookingTransactionsSum ->
                                            balanceWithSnapshotTime
                                                    .getLeft()
                                                    .add(bookingTransactionsSum));
}
