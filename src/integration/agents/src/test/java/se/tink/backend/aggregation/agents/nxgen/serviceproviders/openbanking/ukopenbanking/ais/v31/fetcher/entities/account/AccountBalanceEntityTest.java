package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.fetcher.entities.account;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.api.UkOpenBankingApiDefinitions.CreditDebitIndicator;
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
        Assert.assertEquals(CreditDebitIndicator.CREDIT, creditBalance.getCreditDebitIndicator());
    }

    @Test
    public void shouldMapUppercaseCredit() {
        // given
        AccountBalanceEntity creditBalance = BalanceFixtures.balanceUppercaseCredit();

        // then
        Assert.assertEquals(CreditDebitIndicator.CREDIT, creditBalance.getCreditDebitIndicator());
    }

    @Test
    public void shouldMapLowercaseCredit() {
        // given
        AccountBalanceEntity creditBalance = BalanceFixtures.balanceLowercaseCredit();

        // then
        Assert.assertEquals(CreditDebitIndicator.CREDIT, creditBalance.getCreditDebitIndicator());
    }

    @Test
    public void shouldMapCorrectlyDebitIndicatorAmountAndCurrency() {
        // given
        AccountBalanceEntity debitBalance = BalanceFixtures.balanceDebit();

        // then
        assertThat(debitBalance.getAmount().getExactValue())
                .isEqualByComparingTo(BigDecimal.valueOf(-222.051234d));
        assertThat(debitBalance.getAmount().getCurrencyCode()).isEqualTo("EUR");
        Assert.assertEquals(CreditDebitIndicator.DEBIT, debitBalance.getCreditDebitIndicator());
    }

    @Test
    public void shouldMapUppercaseDebit() {
        // given
        AccountBalanceEntity debitBalance = BalanceFixtures.balanceUppercaseDebit();

        // then
        Assert.assertEquals(CreditDebitIndicator.DEBIT, debitBalance.getCreditDebitIndicator());
    }

    @Test
    public void shouldMapLowercaseDebit() {
        // given
        AccountBalanceEntity debitBalance = BalanceFixtures.balanceLowercaseDebit();

        // then
        Assert.assertEquals(CreditDebitIndicator.DEBIT, debitBalance.getCreditDebitIndicator());
    }

    @Test
    public void shouldMapNullOrEmptyDebitBalance() {
        // given
        AccountBalanceEntity debitBalance = BalanceFixtures.balanceEmpty();

        // then
        Assert.assertNull(debitBalance.getCreditDebitIndicator());
    }
}
