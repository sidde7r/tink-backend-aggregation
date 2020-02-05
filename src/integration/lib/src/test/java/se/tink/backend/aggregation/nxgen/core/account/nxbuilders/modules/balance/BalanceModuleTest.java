package se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import org.junit.Test;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.amount.ExactCurrencyAmount;

@SuppressWarnings("ConstantConditions")
public class BalanceModuleTest {
    @Test
    public void of_exactBalance() {
        ExactCurrencyAmount amount = ExactCurrencyAmount.of(BigDecimal.valueOf(257.90), "SEK");
        BalanceModule balance = BalanceModule.of(amount);

        // Test successful build
        assertEquals(257.90, balance.getExactBalance().getDoubleValue(), 0);
        assertFalse(balance.getExactAvaliableCredit().isPresent());
        assertFalse(balance.getInterestRate().isPresent());
    }

    @Test
    public void of() {
        Amount amount = Amount.inSEK(257.90);
        BalanceModule balance = BalanceModule.of(amount);

        // Test successful build
        assertEquals(257.90, balance.getBalance().doubleValue(), 0);
        assertFalse(balance.getAvailableCredit().isPresent());
        assertFalse(balance.getInterestRate().isPresent());

        // Ensure immutability
        amount.setValue(100);
        assertEquals(257.90, balance.getBalance().doubleValue(), 0);
    }

    @Test(expected = NullPointerException.class)
    public void nullBalanceOf() {
        BalanceModule.of((Amount) null);
    }

    @Test(expected = NullPointerException.class)
    public void nullBalanceOf_exactAmount() {
        BalanceModule.of((ExactCurrencyAmount) null);
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
        Amount bal = Amount.inDKK(25_506.32);
        Amount credit = Amount.inDKK(9473.27);

        BalanceModule balance =
                BalanceModule.builder()
                        .withBalance(bal)
                        .setAvailableCredit(credit)
                        .setInterestRate(0.0265)
                        .build();

        // Try to mutate
        bal.add(Amount.inDKK(20));
        credit.setCurrency("USD");

        assertTrue(balance.getInterestRate().isPresent());
        assertTrue(balance.getAvailableCredit().isPresent());
        assertEquals(25_506.32, balance.getBalance().getValue(), 0);
        assertEquals(9473.27, balance.getAvailableCredit().get().getValue(), 0);
        assertEquals("DKK", balance.getBalance().getCurrency());
        assertEquals("DKK", balance.getAvailableCredit().get().getCurrency());
        assertEquals(0.0265, balance.getInterestRate().get(), 0);
        assertNull(balance.getExactCreditLimit());
        assertNull(balance.getExactAvailableBalance());
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

        assertTrue(balance.getInterestRate().isPresent());
        assertTrue(balance.getAvailableCredit().isPresent());
        assertEquals(25506.32, balance.getBalance().getValue(), 0);
        assertEquals(9473.27, balance.getAvailableCredit().get().getValue(), 0);
        assertEquals("DKK", balance.getBalance().getCurrency());
        assertEquals("DKK", balance.getAvailableCredit().get().getCurrency());
        assertEquals(0.0265, balance.getInterestRate().get(), 0);
        assertEquals(25_006.32, balance.getExactAvailableBalance().getDoubleValue(), 0);
        assertEquals("DKK", balance.getExactAvailableBalance().getCurrencyCode());
        assertEquals(10_000d, balance.getExactCreditLimit().getDoubleValue(), 0);
        assertEquals("DKK", balance.getExactCreditLimit().getCurrencyCode());
    }
}
