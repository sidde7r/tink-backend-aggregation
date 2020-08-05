package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.fetcher.transactionalaccount.entities;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class FabricBalanceTransformTest {

    private static final BalanceEntity closingBooked =
            SerializationUtils.deserializeFromString(
                    "{"
                            + "\"balanceAmount\": {\"currency\": \"EUR\", \"amount\": \"20.00\"},"
                            + "\"balanceType\": \"closingBooked\""
                            + "}",
                    BalanceEntity.class);

    private static final BalanceEntity openingBooked =
            SerializationUtils.deserializeFromString(
                    "{"
                            + "\"balanceAmount\": {\"currency\": \"EUR\", \"amount\": \"30.00\"},"
                            + "\"balanceType\": \"openingBooked\""
                            + "}",
                    BalanceEntity.class);

    private static final BalanceEntity interimAvailable =
            SerializationUtils.deserializeFromString(
                    "{"
                            + "\"balanceAmount\": {\"currency\": \"EUR\", \"amount\": \"40.00\"},"
                            + "\"balanceType\": \"interimAvailable\""
                            + "}",
                    BalanceEntity.class);

    private static final BalanceEntity expected =
            SerializationUtils.deserializeFromString(
                    "{"
                            + "\"balanceAmount\": {\"currency\": \"EUR\", \"amount\": \"50.00\"},"
                            + "\"balanceType\": \"expected\""
                            + "}",
                    BalanceEntity.class);

    private static final BalanceEntity forwardAvailable =
            SerializationUtils.deserializeFromString(
                    "{"
                            + "\"balanceAmount\": {\"currency\": \"EUR\", \"amount\": \"60.00\"},"
                            + "\"balanceType\": \"forwardAvailable\""
                            + "}",
                    BalanceEntity.class);

    @Test
    public void whenBookedBalanceIsMissingExpectException() {
        // given

        // when
        Throwable result =
                Assertions.catchThrowable(
                        () -> FabricBalanceTransform.calculate(Collections.emptyList()));

        // then
        assertThat(result)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Balance is missing.");
    }

    @Test
    public void closingBookedShouldBePreferredOverOpeningBookedWhenCalculatedBookedBalance() {
        // given
        List<BalanceEntity> balances = new ArrayList<>();
        balances.add(closingBooked);
        balances.add(openingBooked);

        // when
        BalanceModule result = FabricBalanceTransform.calculate(balances);

        // then
        assertThat(result.getExactBalance()).isEqualTo(ExactCurrencyAmount.inEUR(20.00));
    }

    @Test
    public void openingBookedShouldBePreferredOverInterimAvailableWhenCalculatedBookedBalance() {
        // given
        List<BalanceEntity> balances = new ArrayList<>();
        balances.add(interimAvailable);
        balances.add(openingBooked);

        // when
        BalanceModule result = FabricBalanceTransform.calculate(balances);

        // then
        assertThat(result.getExactBalance()).isEqualTo(ExactCurrencyAmount.inEUR(30.00));
    }

    @Test
    public void interimAvailableShouldBePreferredOverExpectedWhenCalculatedBookedBalance() {
        // given
        List<BalanceEntity> balances = new ArrayList<>();
        balances.add(interimAvailable);
        balances.add(expected);

        // when
        BalanceModule result = FabricBalanceTransform.calculate(balances);

        // then
        assertThat(result.getExactBalance()).isEqualTo(ExactCurrencyAmount.inEUR(40.00));
    }

    @Test
    public void expectedShouldBeTakenWhenItsOnlyOptionForBookedBalance() {
        // given
        List<BalanceEntity> balances = new ArrayList<>();
        balances.add(expected);

        // when
        BalanceModule result = FabricBalanceTransform.calculate(balances);

        // then
        assertThat(result.getExactBalance()).isEqualTo(ExactCurrencyAmount.inEUR(50.00));
    }

    @Test
    public void interimAvailableShouldBePreferredOverExpectedWhenCalculatedAvailableBalance() {
        // given
        List<BalanceEntity> balances = new ArrayList<>();
        balances.add(interimAvailable);
        balances.add(expected);

        // when
        BalanceModule result = FabricBalanceTransform.calculate(balances);

        // then
        assertThat(result.getExactAvailableBalance()).isEqualTo(ExactCurrencyAmount.inEUR(40.00));
    }

    @Test
    public void expectedShouldBePreferredOverForwardAvailableWhenCalculatedAvailableBalance() {
        // given
        List<BalanceEntity> balances = new ArrayList<>();
        balances.add(forwardAvailable);
        balances.add(expected);

        // when
        BalanceModule result = FabricBalanceTransform.calculate(balances);

        // then
        assertThat(result.getExactAvailableBalance()).isEqualTo(ExactCurrencyAmount.inEUR(50.00));
    }

    @Test
    public void forwardAvailableShouldBeTakenWhenItsOnlyOptionForAvailableBalance() {
        // given
        List<BalanceEntity> balances = new ArrayList<>();
        balances.add(forwardAvailable);
        balances.add(closingBooked);

        // when
        BalanceModule result = FabricBalanceTransform.calculate(balances);

        // then
        assertThat(result.getExactAvailableBalance()).isEqualTo(ExactCurrencyAmount.inEUR(60.00));
    }

    @Test
    public void bothBalancesShouldBeSetIfPossible() {
        // given
        List<BalanceEntity> balances = new ArrayList<>();
        balances.add(forwardAvailable);
        balances.add(closingBooked);

        // when
        BalanceModule result = FabricBalanceTransform.calculate(balances);

        // then
        assertThat(result.getExactBalance()).isEqualTo(ExactCurrencyAmount.inEUR(20.00));
        assertThat(result.getExactAvailableBalance()).isEqualTo(ExactCurrencyAmount.inEUR(60.00));
    }
}
