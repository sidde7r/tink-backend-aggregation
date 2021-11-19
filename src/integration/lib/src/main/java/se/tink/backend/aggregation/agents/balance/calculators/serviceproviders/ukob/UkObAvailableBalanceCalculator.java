package se.tink.backend.aggregation.agents.balance.calculators.serviceproviders.ukob;

import static se.tink.backend.agents.rpc.AccountBalanceType.CLOSING_AVAILABLE;
import static se.tink.backend.agents.rpc.AccountBalanceType.EXPECTED;
import static se.tink.backend.agents.rpc.AccountBalanceType.FORWARD_AVAILABLE;
import static se.tink.backend.agents.rpc.AccountBalanceType.INTERIM_AVAILABLE;
import static se.tink.backend.agents.rpc.AccountBalanceType.OPENING_AVAILABLE;
import static se.tink.backend.aggregation.agents.balance.Calculations.addPendingTransactionsWithBookingDateAfterBalanceSnapshot;
import static se.tink.backend.aggregation.agents.balance.Calculations.returnBalanceAmountAsIs;

import com.google.common.collect.ImmutableList;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import se.tink.backend.agents.rpc.AccountBalanceType;
import se.tink.backend.aggregation.agents.balance.Calculation;
import se.tink.backend.aggregation.agents.balance.calculators.BalanceCalculator;
import se.tink.backend.aggregation.agents.models.Transaction;
import se.tink.libraries.amount.ExactCurrencyAmount;

@Slf4j
@RequiredArgsConstructor
public class UkObAvailableBalanceCalculator {

    private final BalanceCalculator calculator;

    public UkObAvailableBalanceCalculator() {
        this.calculator = new BalanceCalculator();
    }

    private static final List<Pair<AccountBalanceType, Calculation>> prioritizedCalculations =
            ImmutableList.<Pair<AccountBalanceType, Calculation>>builder()
                    .add(Pair.of(EXPECTED, returnBalanceAmountAsIs))
                    .add(Pair.of(INTERIM_AVAILABLE, returnBalanceAmountAsIs))
                    .add(Pair.of(FORWARD_AVAILABLE, returnBalanceAmountAsIs))
                    .add(
                            Pair.of(
                                    CLOSING_AVAILABLE,
                                    addPendingTransactionsWithBookingDateAfterBalanceSnapshot))
                    .add(
                            Pair.of(
                                    OPENING_AVAILABLE,
                                    addPendingTransactionsWithBookingDateAfterBalanceSnapshot))
                    .build();

    public Optional<ExactCurrencyAmount> calculateAvailableBalance(
            Map<AccountBalanceType, Pair<ExactCurrencyAmount, Instant>> granularBalances,
            List<Transaction> transactions) {
        log.info(
                "[UK OB AVAILABLE BALANCE CALCULATOR] Trying to calculate Available Balance if needed and possible");
        return calculator.findFirstPossibleCalculationAndEvaluateIt(
                granularBalances, transactions, prioritizedCalculations);
    }
}
