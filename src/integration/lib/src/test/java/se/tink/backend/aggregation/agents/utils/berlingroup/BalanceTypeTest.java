package se.tink.backend.aggregation.agents.utils.berlingroup;

import java.math.BigDecimal;
import java.util.NoSuchElementException;
import org.junit.Assert;
import org.junit.Test;

public class BalanceTypeTest {

    private static BalanceEntity createBalanceEntity(String type) {
        return new BalanceEntity()
                .setBalanceAmount(
                        new AmountEntity().setAmount(BigDecimal.valueOf(0)).setCurrency("EUR"))
                .setBalanceType(type)
                .setCreditLimitIncluded(false);
    }

    @Test(expected = NoSuchElementException.class)
    public void balanceTypeIsNull() {
        final BalanceType balanceType = createBalanceEntity(null).getBalanceType().get();
    }

    @Test(expected = NoSuchElementException.class)
    public void balanceTypeIsEmptyString() {
        final BalanceType balanceType = createBalanceEntity("").getBalanceType().get();
    }

    @Test
    public void balanceTypeIsValid() {
        Assert.assertEquals(
                BalanceType.INTERIM_BOOKED,
                createBalanceEntity("interimBooked").getBalanceType().get());
    }

    @Test(expected = NoSuchElementException.class)
    public void balanceTypeIsInvalid() {
        final BalanceType balanceType = createBalanceEntity("dummy").getBalanceType().get();
    }
}
