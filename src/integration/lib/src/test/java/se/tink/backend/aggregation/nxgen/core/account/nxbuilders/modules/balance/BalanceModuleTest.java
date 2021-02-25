package se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.math.BigDecimal;
import org.junit.Test;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class BalanceModuleTest {

    @Test
    public void of_exactBalance() {
        ExactCurrencyAmount amount = ExactCurrencyAmount.of(BigDecimal.valueOf(257.90), "SEK");
        BalanceModule balance = BalanceModule.of(amount);

        // Test successful build
        assertEquals(257.90, balance.getExactBalance().getDoubleValue(), 0);
        assertFalse(balance.getExactAvailableCredit().isPresent());
        assertFalse(balance.getInterestRate().isPresent());
    }

    @Test
    public void of() {
        ExactCurrencyAmount amount = ExactCurrencyAmount.inSEK(257.90);
        BalanceModule balance = BalanceModule.of(amount);

        // Test successful build
        assertThat(balance.getExactBalance().getDoubleValue()).isEqualTo(257.90);
        assertThat(balance.getAvailableCredit().isPresent()).isFalse();
        assertThat(balance.getInterestRate().isPresent()).isFalse();
    }

    @Test(expected = NullPointerException.class)
    public void nullBalanceOf() {
        BalanceModule.of(null);
    }

    @Test(expected = NullPointerException.class)
    public void nullBalanceOf_exactAmount() {
        BalanceModule.of(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void negativeInterest() {
        BalanceModule.builder()
                .withBalance(ExactCurrencyAmount.of(BigDecimal.valueOf(20), "SEK"))
                .setAvailableCredit(ExactCurrencyAmount.of(BigDecimal.valueOf(10_000), "SEK"))
                .setInterestRate(-0.25)
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void negativeCreditLimit() {
        BalanceModule.builder()
                .withBalance(ExactCurrencyAmount.of(BigDecimal.valueOf(20), "SEK"))
                .setCreditLimit(ExactCurrencyAmount.of(BigDecimal.valueOf(-10_000), "SEK"))
                .build();
    }

    @Test
    public void successfulBuild() {
        ExactCurrencyAmount bal = ExactCurrencyAmount.inDKK(25_506.32);
        ExactCurrencyAmount credit = ExactCurrencyAmount.inDKK(9473.27);

        BalanceModule balance =
                BalanceModule.builder()
                        .withBalance(bal)
                        .setAvailableCredit(credit)
                        .setInterestRate(0.0265)
                        .build();

        // Try to mutate
        bal.add(ExactCurrencyAmount.inDKK(20));

        assertThat(balance.getInterestRate().isPresent()).isTrue();
        assertThat(balance.getAvailableCredit().isPresent()).isTrue();
        assertThat(balance.getExactBalance().getDoubleValue()).isEqualTo(25_506.32);
        assertThat(balance.getAvailableCredit().get().getValue()).isEqualTo(9473.27);
        assertThat(balance.getExactBalance().getCurrencyCode()).isEqualTo("DKK");
        assertThat(balance.getExactAvailableCredit().get().getCurrencyCode()).isEqualTo("DKK");
        assertThat(balance.getInterestRate().get()).isEqualTo(0.0265);
        assertThat(balance.getExactCreditLimit()).isNull();
        assertThat(balance.getExactAvailableBalance()).isNull();
    }

    @Test
    public void successfulBuildWithExactCurrencyAmount() {
        ExactCurrencyAmount bal = ExactCurrencyAmount.of(BigDecimal.valueOf(25506.32), "DKK");
        ExactCurrencyAmount credit = ExactCurrencyAmount.of(BigDecimal.valueOf(9473.27), "DKK");
        ExactCurrencyAmount availableBalance = ExactCurrencyAmount.of(25_006.32, "DKK");
        ExactCurrencyAmount creditLimit = ExactCurrencyAmount.of(10_000d, "DKK");

        BalanceModule balance =
                BalanceModule.builder()
                        .withBalance(bal)
                        .setAvailableCredit(credit)
                        .setAvailableBalance(availableBalance)
                        .setCreditLimit(creditLimit)
                        .setInterestRate(0.0265)
                        .build();

        assertThat(balance.getInterestRate().isPresent()).isTrue();
        assertThat(balance.getAvailableCredit().isPresent()).isTrue();
        assertThat(balance.getExactBalance().getDoubleValue()).isEqualTo(25506.32);
        assertThat(balance.getAvailableCredit().get().getValue()).isEqualTo(9473.27);
        assertThat(balance.getExactBalance().getCurrencyCode()).isEqualTo("DKK");
        assertThat(balance.getAvailableCredit().get().getCurrency()).isEqualTo("DKK");
        assertThat(balance.getInterestRate().get()).isEqualTo(0.0265);
        assertThat(balance.getExactAvailableBalance().getDoubleValue()).isEqualTo(25_006.32);
        assertThat(balance.getExactAvailableBalance().getCurrencyCode()).isEqualTo("DKK");
        assertThat(balance.getExactCreditLimit().getDoubleValue()).isEqualTo(10_000d);
        assertThat(balance.getExactCreditLimit().getCurrencyCode()).isEqualTo("DKK");
    }
}
