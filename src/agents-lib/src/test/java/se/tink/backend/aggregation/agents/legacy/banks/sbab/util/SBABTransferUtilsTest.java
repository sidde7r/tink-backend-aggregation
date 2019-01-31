package se.tink.backend.aggregation.agents.banks.sbab.util;

import org.junit.Assert;
import org.junit.Test;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.transfer.rpc.Transfer;

public class SBABTransferUtilsTest {

    @Test
    public void threeDecimalsInAmount_ReturnsTwoDecimals() {
        Transfer transfer = new Transfer();
        transfer.setAmount(new Amount(null, 100.123));
        Assert.assertEquals("100,12", SBABTransferUtils.formatAmount(transfer));
    }

    @Test
    public void twoDecimalsInAmount_ReturnsTwoDecimals() {
        Transfer transfer = new Transfer();
        transfer.setAmount(new Amount(null, 100.12));
        Assert.assertEquals("100,12", SBABTransferUtils.formatAmount(transfer));
    }

    @Test
    public void oneDecimalInAmount_ReturnsTwoDecimals() {
        Transfer transfer = new Transfer();
        transfer.setAmount(new Amount(null, 100.1));
        Assert.assertEquals("100,10", SBABTransferUtils.formatAmount(transfer));
    }

    @Test
    public void noDecimalsInAmount_ReturnsTwoDecimals() {
        Transfer transfer = new Transfer();
        transfer.setAmount(new Amount(null, 100.0));
        Assert.assertEquals("100,00", SBABTransferUtils.formatAmount(transfer));
    }

    @Test
    public void threeDecimalsInAmount_ReturnsTwo_AndRoundsUp() {
        Transfer transfer = new Transfer();
        transfer.setAmount(new Amount(null, 100.567));
        Assert.assertEquals("100,57", SBABTransferUtils.formatAmount(transfer));
    }
    
    @Test
    public void amountBetweenZeroAndOne_LeadingZero() {
        Transfer transfer = new Transfer();
        transfer.setAmount(new Amount(null, 0.12));
        Assert.assertEquals("0,12", SBABTransferUtils.formatAmount(transfer));
    }
    
    @Test
    public void amountBelowZero_LeadingMinus() {
        Transfer transfer = new Transfer();
        transfer.setAmount(new Amount(null, -123.45));
        Assert.assertEquals("-123,45", SBABTransferUtils.formatAmount(transfer));
    }
}
