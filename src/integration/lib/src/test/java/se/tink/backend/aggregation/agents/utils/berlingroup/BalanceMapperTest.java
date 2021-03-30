package se.tink.backend.aggregation.agents.utils.berlingroup;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import com.google.common.collect.ImmutableList;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;
import se.tink.libraries.amount.ExactCurrencyAmount;

@RunWith(JUnitParamsRunner.class)
public class BalanceMapperTest {

    private static final BalanceEntity CLOSING_BOOKED_WITHOUT_LIMIT =
            withoutCreditLimit(BalanceType.CLOSING_BOOKED.getType(), 111.11);
    private static final BalanceEntity CLOSING_BOOKED_WITH_LIMIT =
            withCreditLimit(BalanceType.CLOSING_BOOKED.getType(), 100111.11);
    private static final BalanceEntity EXPECTED_WITHOUT_LIMIT =
            withoutCreditLimit(BalanceType.EXPECTED.getType(), 222.22);
    private static final BalanceEntity EXPECTED_WITH_LIMIT =
            withCreditLimit(BalanceType.EXPECTED.getType(), 200222.22);
    private static final BalanceEntity OPENING_BOOKED_WITHOUT_LIMIT =
            withoutCreditLimit(BalanceType.OPENING_BOOKED.getType(), 333.33);
    private static final BalanceEntity OPENING_BOOKED_WITH_LIMIT =
            withCreditLimit(BalanceType.OPENING_BOOKED.getType(), 300333.33);
    private static final BalanceEntity INTERIM_AVAILABLE_WITHOUT_LIMIT =
            withoutCreditLimit(BalanceType.INTERIM_AVAILABLE.getType(), 444.44);
    private static final BalanceEntity INTERIM_AVAILABLE_WITH_LIMIT =
            withCreditLimit(BalanceType.INTERIM_AVAILABLE.getType(), 400444.44);
    private static final BalanceEntity INTERIM_BOOKED_WITHOUT_LIMIT =
            withoutCreditLimit(BalanceType.INTERIM_BOOKED.getType(), 555.55);
    private static final BalanceEntity INTERIM_BOOKED_WITH_LIMIT =
            withCreditLimit(BalanceType.INTERIM_BOOKED.getType(), 500555.55);
    private static final BalanceEntity FORWARD_AVAILABLE_WITHOUT_LIMIT =
            withoutCreditLimit(BalanceType.FORWARD_AVAILABLE.getType(), 666.66);
    private static final BalanceEntity FORWARD_AVAILABLE_WITH_LIMIT =
            withCreditLimit(BalanceType.FORWARD_AVAILABLE.getType(), 600666.66);
    private static final BalanceEntity NON_INVOICED_WITHOUT_LIMIT =
            withoutCreditLimit(BalanceType.NON_INVOICED.getType(), 777.77);
    private static final BalanceEntity NON_INVOICED_WITH_LIMIT =
            withCreditLimit(BalanceType.NON_INVOICED.getType(), 700777.77);
    private static final BalanceEntity UNKNOWN_WITHOUT_LIMIT =
            withoutCreditLimit("unknown", 888.88);
    private static final BalanceEntity UNKNOWN_WITH_LIMIT = withCreditLimit("unknown", 800888.88);

    private static BalanceEntity withCreditLimit(String type, double amount) {
        return new BalanceEntity()
                .setBalanceAmount(
                        new AmountEntity().setAmount(BigDecimal.valueOf(amount)).setCurrency("EUR"))
                .setBalanceType(type)
                .setCreditLimitIncluded(true);
    }

    private static BalanceEntity withoutCreditLimit(String type, double amount) {
        return new BalanceEntity()
                .setBalanceAmount(
                        new AmountEntity().setAmount(BigDecimal.valueOf(amount)).setCurrency("EUR"))
                .setBalanceType(type)
                .setCreditLimitIncluded(false);
    }

    @Test
    public void shouldReturnFirstSuppliedBalanceIfNoneFitsBookedBalancePriorities() {
        ExactCurrencyAmount bookedBalance =
                BalanceMapper.getBookedBalance(
                        ImmutableList.of(
                                CLOSING_BOOKED_WITH_LIMIT,
                                EXPECTED_WITH_LIMIT,
                                OPENING_BOOKED_WITH_LIMIT,
                                INTERIM_AVAILABLE_WITH_LIMIT,
                                INTERIM_BOOKED_WITH_LIMIT,
                                FORWARD_AVAILABLE_WITH_LIMIT,
                                NON_INVOICED_WITH_LIMIT,
                                UNKNOWN_WITH_LIMIT));

        assertThat(bookedBalance).isEqualTo(CLOSING_BOOKED_WITH_LIMIT.toTinkAmount());
    }

    private Object[] bookedBalancePriorityParameters() {
        return new Object[] {
            new Object[] {
                new BalanceEntity[] {
                    INTERIM_AVAILABLE_WITHOUT_LIMIT,
                    EXPECTED_WITHOUT_LIMIT,
                    CLOSING_BOOKED_WITHOUT_LIMIT,
                    OPENING_BOOKED_WITHOUT_LIMIT,
                    INTERIM_BOOKED_WITHOUT_LIMIT
                },
                INTERIM_BOOKED_WITHOUT_LIMIT
            },
            new Object[] {
                new BalanceEntity[] {
                    INTERIM_AVAILABLE_WITHOUT_LIMIT,
                    EXPECTED_WITHOUT_LIMIT,
                    CLOSING_BOOKED_WITHOUT_LIMIT,
                    OPENING_BOOKED_WITHOUT_LIMIT,
                    INTERIM_BOOKED_WITH_LIMIT
                },
                OPENING_BOOKED_WITHOUT_LIMIT
            }
        };
    }

    @Test
    @Parameters(method = "bookedBalancePriorityParameters")
    public void bookedBalanceShouldPrioritizeCorrectly(
            BalanceEntity[] balances, BalanceEntity expected) {
        ExactCurrencyAmount bookedBalance = BalanceMapper.getBookedBalance(Arrays.asList(balances));

        assertThat(bookedBalance).isEqualTo(expected.toTinkAmount());
    }

    @Test
    public void bookedBalanceShouldThrowIllegalArgumentExceptionWhenProvidedWithEmptyList() {
        Throwable throwable =
                catchThrowable(() -> BalanceMapper.getBookedBalance(Collections.emptyList()));

        assertThat(throwable)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cannot determine booked balance from empty list of balances.");
    }

    @Test
    public void availableBalanceShouldReturnEmptyOptionalWhenProvidedWithEmptyList() {
        Optional<ExactCurrencyAmount> availableBalance =
                BalanceMapper.getAvailableBalance(Collections.emptyList());

        assertThat(availableBalance.isPresent()).isFalse();
    }

    @Test
    public void creditLimitShouldReturnEmptyOptionalWhenProvidedWithEmptyList() {
        Optional<ExactCurrencyAmount> creditLimit =
                BalanceMapper.getCreditLimit(Collections.emptyList());

        assertThat(creditLimit.isPresent()).isFalse();
    }

    @Test
    public void
            creditLimitShouldReturnEmptyOptionalWhenProvidedWithSameTypeOnMoreThanTwoBalances() {
        Optional<ExactCurrencyAmount> creditLimit =
                BalanceMapper.getCreditLimit(
                        ImmutableList.of(
                                CLOSING_BOOKED_WITHOUT_LIMIT,
                                CLOSING_BOOKED_WITH_LIMIT,
                                CLOSING_BOOKED_WITHOUT_LIMIT));

        assertThat(creditLimit.isPresent()).isFalse();
    }

    @Test
    public void
            creditLimitShouldReturnEmptyOptionalWhenProvidedWithSameTypeBalancesWithSameFlags() {

        Optional<ExactCurrencyAmount> creditLimit =
                BalanceMapper.getCreditLimit(
                        ImmutableList.of(
                                CLOSING_BOOKED_WITHOUT_LIMIT, CLOSING_BOOKED_WITHOUT_LIMIT));

        assertThat(creditLimit.isPresent()).isFalse();
    }

    @Test
    public void
            creditLimitShouldReturnDifferenceBetweenTwoFoundBalancesOfSameTypeWithProperFlags() {

        Optional<ExactCurrencyAmount> creditLimit =
                BalanceMapper.getCreditLimit(
                        ImmutableList.of(CLOSING_BOOKED_WITHOUT_LIMIT, CLOSING_BOOKED_WITH_LIMIT));
        assertThat(creditLimit.isPresent()).isTrue();
        assertThat(creditLimit.get()).isEqualTo(ExactCurrencyAmount.of("100000", "EUR"));
    }

    @Test
    public void
            creditLimitShouldReturnDifferenceBetweenTwoFoundBalancesOfSameTypeWhenWithExtraBalances() {

        Optional<ExactCurrencyAmount> creditLimit =
                BalanceMapper.getCreditLimit(
                        ImmutableList.of(
                                FORWARD_AVAILABLE_WITHOUT_LIMIT,
                                INTERIM_AVAILABLE_WITH_LIMIT,
                                EXPECTED_WITH_LIMIT,
                                FORWARD_AVAILABLE_WITH_LIMIT));
        assertThat(creditLimit.isPresent()).isTrue();
        assertThat(creditLimit.get()).isEqualTo(ExactCurrencyAmount.of("600000", "EUR"));
    }

    @Test
    public void creditLimitShouldReturnEmptyOptionalIfNoTwoBalancesWithTheSameTypeProvided() {
        Optional<ExactCurrencyAmount> creditLimit =
                BalanceMapper.getCreditLimit(
                        ImmutableList.of(
                                INTERIM_AVAILABLE_WITH_LIMIT,
                                EXPECTED_WITH_LIMIT,
                                CLOSING_BOOKED_WITH_LIMIT));
        assertThat(creditLimit.isPresent()).isFalse();
    }
}
