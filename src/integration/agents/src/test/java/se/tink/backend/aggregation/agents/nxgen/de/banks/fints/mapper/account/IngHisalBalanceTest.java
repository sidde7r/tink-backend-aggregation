package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.mapper.account;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.response.HISAL;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class IngHisalBalanceTest {

    private IngHisalBalance balance;

    @Before
    public void setUp() {
        balance = new IngHisalBalance();
    }

    @Test
    public void calculateShouldMapBookedAsExactAndPendingAsAvailable() {
        // given

        // when
        BalanceModule result =
                balance.calculate(
                        new HISAL()
                                .setCurrency("EUR")
                                .setFirstBalanceValue(BigDecimal.valueOf(10))
                                .setSecondBalanceValue(BigDecimal.valueOf(3)));

        // then
        assertThat(result.getExactBalance())
                .isEqualTo(ExactCurrencyAmount.of(BigDecimal.valueOf(10), "EUR"));
        assertThat(result.getExactAvailableBalance())
                .isEqualTo(ExactCurrencyAmount.of(BigDecimal.valueOf(3), "EUR"));
    }
}
