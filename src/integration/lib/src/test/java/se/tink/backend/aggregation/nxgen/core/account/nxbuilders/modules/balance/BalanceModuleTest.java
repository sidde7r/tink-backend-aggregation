package se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import se.tink.libraries.amount.Amount;

@SuppressWarnings("ConstantConditions")
public class BalanceModuleTest {

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
        BalanceModule.of(null);
    }

    @Test(expected = NullPointerException.class)
    public void nullBalance() {
        BalanceModule.builder().withBalance(null).build();
    }

    @Test(expected = NullPointerException.class)
    public void nullCredit() {
        BalanceModule.builder().withBalance(Amount.inSEK(20)).setAvailableCredit(null).build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void negativeInterest() {
        BalanceModule.builder()
                .withBalance(Amount.inSEK(20))
                .setAvailableCredit(Amount.inSEK(10_000))
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
}
