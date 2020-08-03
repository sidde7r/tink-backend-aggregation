package src.integration.transfer.src.test.java.se.tink.libraries.transfer.rpc;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

import org.junit.Test;
import se.tink.libraries.transfer.rpc.RemittanceInformation;
import se.tink.libraries.transfer.rpc.Transfer;

public class TransferTest {

    @Test
    public void isRemittanceInformationGenerated_should_handleNullRemittanceInformation() {
        final Transfer transfer = new Transfer();

        assertNull(transfer.getRemittanceInformation());
        assertFalse(transfer.isRemittanceInformationGenerated());
    }

    @Test
    public void isRemittanceInformationGenerated_should_handleNullRemittanceInformationValue() {
        Transfer transfer = new Transfer();
        transfer.setRemittanceInformation(new RemittanceInformation());

        assertNull(transfer.getRemittanceInformation().getValue());
        assertFalse(transfer.isRemittanceInformationGenerated());
    }

    @Test
    public void isDestinationMessageGenerated_should_handleNullDestinationMessage() {
        final Transfer transfer = new Transfer();

        assertFalse(transfer.isDestinationMessageGenerated());
    }
}
