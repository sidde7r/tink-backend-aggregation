package se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance;

import org.junit.Test;
import se.tink.backend.agents.rpc.ExactCurrencyAmount;
import se.tink.libraries.amount.Amount;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@SuppressWarnings("ConstantConditions")
public class BalanceModuleTest {
    @Test
    public void of_exactBalance() {
        ExactCurrencyAmount amount = ExactCurrencyAmount.of(257.90, "SEK");
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
                .withBalance(ExactCurrencyAmount.of(20, "SEK"))
                .setAvailableCredit(ExactCurrencyAmount.of(10_000, "SEK"))
                .setInterestRate(-0.25)
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
    }

    @Test
    public void successfulBuildWithExactCurrencyAmount() {
        ExactCurrencyAmount bal = ExactCurrencyAmount.of(25506.32, "DKK");
        ExactCurrencyAmount credit = ExactCurrencyAmount.of(9473.27, "DKK");

        BalanceModule balance =
                BalanceModule.builder()
                        .withBalance(bal)
                        .setAvailableCredit(credit)
                        .setInterestRate(0.0265)
                        .build();

        assertTrue(balance.getInterestRate().isPresent());
        assertTrue(balance.getAvailableCredit().isPresent());
        assertEquals(25506.32, balance.getBalance().getValue(), 0);
        assertEquals(9473.27, balance.getAvailableCredit().get().getValue(), 0);
        assertEquals("DKK", balance.getBalance().getCurrency());
        assertEquals("DKK", balance.getAvailableCredit().get().getCurrency());
        assertEquals(0.0265, balance.getInterestRate().get(), 0);
    }
}
