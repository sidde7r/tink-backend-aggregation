package se.tink.backend.core.account;


import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.tink.libraries.account.AccountIdentifier;

import java.util.UUID;

public class TransferDestinationPatternTest {
    TransferDestinationPattern a1;
    TransferDestinationPattern a2;
    TransferDestinationPattern a3;
    TransferDestinationPattern a4;
    TransferDestinationPattern a5;
    TransferDestinationPattern a6;


    @Before
    public void setUp() {
        a1 = new TransferDestinationPattern();
        a1.setUserId(UUID.randomUUID());
        a1.setAccountId(UUID.randomUUID());
        a1.setType(AccountIdentifier.Type.SE);
        a1.setPattern(".+");

        a2 = new TransferDestinationPattern();
        a2.setUserId(a1.getUserId());
        a2.setAccountId(a1.getAccountId());
        a2.setType(AccountIdentifier.Type.SE);
        a2.setPattern("6152135858123");

        a3 = new TransferDestinationPattern();
        a3.setUserId(a1.getUserId());
        a3.setAccountId(a1.getAccountId());
        a3.setType(AccountIdentifier.Type.SE);
        a3.setPattern(".+");

        a4 = new TransferDestinationPattern();
        a4.setUserId(a1.getUserId());
        a4.setAccountId(a1.getAccountId());
        a4.setType(AccountIdentifier.Type.FI);
        a4.setPattern(".+");

        a5 = new TransferDestinationPattern();
        a5.setUserId(a1.getUserId());
        a5.setAccountId(a1.getAccountId());
        a5.setType(AccountIdentifier.Type.SE);
        a5.setPattern(".+");

        a6 = new TransferDestinationPattern();
        a6.setUserId(UUID.randomUUID());
        a6.setAccountId(UUID.randomUUID());
        a6.setType(AccountIdentifier.Type.SE);
        a6.setPattern(".+");
    }

    @Test
    public void testEquality() {
        Assert.assertNotEquals(a1, a2);
        Assert.assertEquals(a1, a3);
        Assert.assertNotEquals(a1, a4);
        Assert.assertEquals(a1, a5);
        Assert.assertNotEquals(a1, a6);
    }

    @Test
    public void testComparison() {
        Assert.assertTrue(a1.compareTo(a2) == -1);
        Assert.assertTrue(a1.compareTo(a3) == 0);
        Assert.assertTrue(a1.compareTo(a4) == 1);
        Assert.assertTrue(a1.compareTo(a5) == 0);
    }
}
