package src.integration.transfer.src.test.java.se.tink.libraries.transfer.rpc;

import static org.junit.Assert.assertNull;

import org.junit.Test;
import se.tink.libraries.transfer.rpc.RemittanceInformation;
import se.tink.libraries.transfer.rpc.Transfer;

public class TransferTest {

    @Test
    public void isRemittanceInformationGeneratedShouldReturnFalseWhenNullRemittanceInformation() {
        final Transfer transfer = new Transfer();

        assertNull(transfer.getRemittanceInformation());
    }

    @Test
    public void
            isRemittanceInformationGeneratedShouldReturnFalseWhenNullRemittanceInformationValue() {
        Transfer transfer = new Transfer();
        transfer.setRemittanceInformation(new RemittanceInformation());

        assertNull(transfer.getRemittanceInformation().getValue());
    }
}
