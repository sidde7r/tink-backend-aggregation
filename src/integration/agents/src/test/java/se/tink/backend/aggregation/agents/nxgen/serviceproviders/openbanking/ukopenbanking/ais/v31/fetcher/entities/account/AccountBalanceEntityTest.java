package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.fetcher.entities.account;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.AccountBalanceEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.fixtures.BalanceFixtures;

public class AccountBalanceEntityTest {

    @Test
    public void creditAmountAndCurrencyIsCorrectlyMapped() {
        // given
        AccountBalanceEntity creditBalance = BalanceFixtures.balanceCredit();

        // then
        assertThat(creditBalance.getAmount().getExactValue())
                .isEqualByComparingTo(BigDecimal.valueOf(111.05d));
        assertThat(creditBalance.getAmount().getCurrencyCode()).isEqualTo("GBP");
    }

    @Test
    public void debitAmountAndCurrencyIsCorrectlyMapped() {
        // given
        AccountBalanceEntity debitBalance = BalanceFixtures.balanceDebit();

        // then
        assertThat(debitBalance.getAmount().getExactValue())
                .isEqualByComparingTo(BigDecimal.valueOf(-222.051234d));
        assertThat(debitBalance.getAmount().getCurrencyCode()).isEqualTo("EUR");
    }
}
