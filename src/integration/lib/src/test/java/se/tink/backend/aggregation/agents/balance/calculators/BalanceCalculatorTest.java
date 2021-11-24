package se.tink.backend.aggregation.agents.balance.calculators;

import static org.assertj.core.api.Assertions.assertThat;
import static se.tink.backend.agents.rpc.AccountBalanceType.CLEARED_BALANCE;
import static se.tink.backend.agents.rpc.AccountBalanceType.CLOSING_AVAILABLE;
import static se.tink.backend.agents.rpc.AccountBalanceType.CLOSING_BOOKED;
import static se.tink.backend.agents.rpc.AccountBalanceType.EXPECTED;
import static se.tink.backend.agents.rpc.AccountBalanceType.FORWARD_AVAILABLE;
import static se.tink.backend.agents.rpc.AccountBalanceType.INFORMATION;
import static se.tink.backend.agents.rpc.AccountBalanceType.INTERIM_BOOKED;
import static se.tink.backend.agents.rpc.AccountBalanceType.INTERIM_CLEARED;
import static se.tink.backend.agents.rpc.AccountBalanceType.OPENING_AVAILABLE;
import static se.tink.backend.agents.rpc.AccountBalanceType.PREVIOUSLY_CLOSED_BOOKED;
import static se.tink.backend.aggregation.agents.balance.Calculations.addBookedTransactionsWithBookingDateAfterBalanceSnapshot;
import static se.tink.backend.aggregation.agents.balance.Calculations.addPendingTransactionsWithBookingDateAfterBalanceSnapshot;
import static se.tink.backend.aggregation.agents.balance.Calculations.returnBalanceAmountAsIs;
import static se.tink.backend.aggregation.agents.balance.Calculations.subtractPendingTransactions;
import static se.tink.backend.aggregation.agents.balance.Calculations.subtractPendingTransactionsWithBookingDateAfterBalanceSnapshot;

import com.google.common.collect.ImmutableList;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;
import org.junit.runner.RunWith;
import se.tink.backend.agents.rpc.AccountBalanceType;
import se.tink.backend.aggregation.agents.balance.Calculation;
import se.tink.libraries.amount.ExactCurrencyAmount;

@SuppressWarnings("unused")
@RunWith(JUnitParamsRunner.class)
public class BalanceCalculatorTest {

    private static final List<Pair<AccountBalanceType, Calculation>>
            prioritizedCalculationsExample =
                    ImmutableList.<Pair<AccountBalanceType, Calculation>>builder()
                            .add(Pair.of(INTERIM_BOOKED, returnBalanceAmountAsIs))
                            .add(Pair.of(INTERIM_CLEARED, returnBalanceAmountAsIs))
                            .add(Pair.of(EXPECTED, subtractPendingTransactions))
                            .add(
                                    Pair.of(
                                            CLOSING_AVAILABLE,
                                            addPendingTransactionsWithBookingDateAfterBalanceSnapshot))
                            .add(
                                    Pair.of(
                                            CLOSING_BOOKED,
                                            subtractPendingTransactionsWithBookingDateAfterBalanceSnapshot))
                            .add(
                                    Pair.of(
                                            OPENING_AVAILABLE,
                                            addBookedTransactionsWithBookingDateAfterBalanceSnapshot))
                            .build();

    private final BalanceCalculator calculator = new BalanceCalculator();

    @Test
    @Parameters(method = "onlyBalancesThatCanNotBeInputForExampleCalculationList")
    public void shouldReturnEmptyOptionalWhenCalculationNotFound(
            Map<AccountBalanceType, Pair<ExactCurrencyAmount, Instant>> granularBalances) {
        // when
        Optional<Pair<AccountBalanceType, Calculation>> balanceTypeWithCalculation =
                calculator.findFirstPossibleCalculation(
                        granularBalances, prioritizedCalculationsExample);

        // then
        assertThat(balanceTypeWithCalculation).isNotPresent();
    }

    @Test
    @Parameters(method = "alwaysContainsExpectedAndNoInterimBalances")
    public void shouldFindFirstPossibleCalculation(
            Map<AccountBalanceType, Pair<ExactCurrencyAmount, Instant>> granularBalances) {
        // when
        Optional<Pair<AccountBalanceType, Calculation>> balanceTypeWithCalculation =
                calculator.findFirstPossibleCalculation(
                        granularBalances, prioritizedCalculationsExample);

        // then
        assertThat(balanceTypeWithCalculation).isPresent();
        assertThat(balanceTypeWithCalculation.get().getLeft()).isEqualTo(EXPECTED);
        assertThat(balanceTypeWithCalculation.get().getRight())
                .isEqualTo(subtractPendingTransactions);
    }

    private Object[] onlyBalancesThatCanNotBeInputForExampleCalculationList() {
        return new Object[] {
            new HashMap<AccountBalanceType, Pair<ExactCurrencyAmount, Instant>>() {
                {
                    put(CLEARED_BALANCE, null);
                    put(INFORMATION, null);
                }
            },
            new HashMap<AccountBalanceType, Pair<ExactCurrencyAmount, Instant>>() {
                {
                    put(FORWARD_AVAILABLE, null);
                    put(INFORMATION, null);
                    put(PREVIOUSLY_CLOSED_BOOKED, null);
                }
            }
        };
    }

    private Object[] alwaysContainsExpectedAndNoInterimBalances() {
        return new Object[] {
            new HashMap<AccountBalanceType, Pair<ExactCurrencyAmount, Instant>>() {
                {
                    put(FORWARD_AVAILABLE, null);
                    put(EXPECTED, null);
                    put(INFORMATION, null);
                }
            },
            new HashMap<AccountBalanceType, Pair<ExactCurrencyAmount, Instant>>() {
                {
                    put(CLOSING_BOOKED, null);
                    put(PREVIOUSLY_CLOSED_BOOKED, null);
                    put(EXPECTED, null);
                }
            },
            new HashMap<AccountBalanceType, Pair<ExactCurrencyAmount, Instant>>() {
                {
                    put(CLOSING_AVAILABLE, null);
                    put(CLOSING_BOOKED, null);
                    put(EXPECTED, null);
                    put(OPENING_AVAILABLE, null);
                }
            }
        };
    }
}
