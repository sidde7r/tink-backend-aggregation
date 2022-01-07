package se.tink.backend.aggregation.agents.balance.calculators.serviceproviders.ukob;

import static se.tink.backend.agents.rpc.AccountBalanceType.CLOSING_AVAILABLE;
import static se.tink.backend.agents.rpc.AccountBalanceType.CLOSING_BOOKED;
import static se.tink.backend.agents.rpc.AccountBalanceType.CLOSING_CLEARED;
import static se.tink.backend.agents.rpc.AccountBalanceType.EXPECTED;
import static se.tink.backend.agents.rpc.AccountBalanceType.INTERIM_BOOKED;
import static se.tink.backend.agents.rpc.AccountBalanceType.INTERIM_CLEARED;
import static se.tink.backend.agents.rpc.AccountBalanceType.OPENING_AVAILABLE;
import static se.tink.backend.agents.rpc.AccountBalanceType.OPENING_BOOKED;
import static se.tink.backend.agents.rpc.AccountBalanceType.OPENING_CLEARED;
import static se.tink.backend.aggregation.agents.balance.Calculations.addBookedTransactionsWithBookingDateAfterBalanceSnapshot;
import static se.tink.backend.aggregation.agents.balance.Calculations.returnBalanceAmountAsIs;
import static se.tink.backend.aggregation.agents.balance.Calculations.subtractPendingTransactions;

import com.google.common.collect.ImmutableList;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.lang3.tuple.Pair;
import se.tink.backend.agents.rpc.AccountBalanceType;
import se.tink.backend.aggregation.agents.balance.Calculation;
import se.tink.backend.aggregation.agents.balance.calculators.BalanceCalculator;
import se.tink.backend.aggregation.agents.balance.calculators.BalanceCalculatorSummary;
import se.tink.backend.aggregation.agents.balance.calculators.BookedBalanceCalculator;
import se.tink.backend.aggregation.agents.models.Transaction;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class UkObBookedBalanceCalculator implements BookedBalanceCalculator {

    private final BalanceCalculator calculator;

    public UkObBookedBalanceCalculator() {
        this.calculator = new BalanceCalculator();
    }

    private static final List<Pair<AccountBalanceType, Calculation>> prioritizedCalculations =
            ImmutableList.<Pair<AccountBalanceType, Calculation>>builder()
                    .add(Pair.of(INTERIM_BOOKED, returnBalanceAmountAsIs))
                    .add(Pair.of(INTERIM_CLEARED, returnBalanceAmountAsIs))
                    .add(Pair.of(EXPECTED, subtractPendingTransactions))
                    .add(
                            Pair.of(
                                    CLOSING_BOOKED,
                                    addBookedTransactionsWithBookingDateAfterBalanceSnapshot))
                    .add(
                            Pair.of(
                                    OPENING_BOOKED,
                                    addBookedTransactionsWithBookingDateAfterBalanceSnapshot))
                    .add(
                            Pair.of(
                                    CLOSING_CLEARED,
                                    addBookedTransactionsWithBookingDateAfterBalanceSnapshot))
                    .add(
                            Pair.of(
                                    OPENING_CLEARED,
                                    addBookedTransactionsWithBookingDateAfterBalanceSnapshot))
                    .add(
                            Pair.of(
                                    CLOSING_AVAILABLE,
                                    addBookedTransactionsWithBookingDateAfterBalanceSnapshot))
                    .add(
                            Pair.of(
                                    OPENING_AVAILABLE,
                                    addBookedTransactionsWithBookingDateAfterBalanceSnapshot))
                    .build();

    public Pair<Optional<ExactCurrencyAmount>, BalanceCalculatorSummary> calculateBookedBalance(
            Map<AccountBalanceType, Pair<ExactCurrencyAmount, Instant>> granularBalances,
            List<Transaction> transactions) {
        return calculator.findFirstPossibleCalculationAndEvaluateIt(
                granularBalances, transactions, prioritizedCalculations);
    }
}
