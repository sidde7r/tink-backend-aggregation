package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.mapper.account;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.response.HISAL;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class DefaultHisalBalanceTest {

    private DefaultHisalBalance balance;

    @Before
    public void setUp() {
        balance = new DefaultHisalBalance();
    }

    @Test
    public void calculateShouldMapBookedAsExactAndBookedAddedPendingAsAvailableBalance() {
        // given

        // when
        BalanceModule result =
                balance.calculate(
                        new HISAL()
                                .setCurrency("EUR")
                                .setBookedBalance(BigDecimal.valueOf(10))
                                .setPendingBalance(BigDecimal.valueOf(3)));

        // then
        assertThat(result.getExactBalance())
                .isEqualTo(ExactCurrencyAmount.of(BigDecimal.valueOf(10), "EUR"));
        assertThat(result.getExactAvailableBalance())
                .isEqualTo(ExactCurrencyAmount.of(BigDecimal.valueOf(13), "EUR"));
    }

    @Test
    public void
            calculateShouldMapBookedAsExactAndBookedAsAvailableBalanceWhenHisalPendingIsMissing() {
        // given

        // when
        BalanceModule result =
                balance.calculate(
                        new HISAL().setCurrency("EUR").setBookedBalance(BigDecimal.valueOf(10)));

        // then
        assertThat(result.getExactBalance())
                .isEqualTo(ExactCurrencyAmount.of(BigDecimal.valueOf(10), "EUR"));
        assertThat(result.getExactAvailableBalance())
                .isEqualTo(ExactCurrencyAmount.of(BigDecimal.valueOf(10), "EUR"));
    }
}
