package src.integration.transfer.src.test.java.se.tink.libraries.transfer.rpc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;
import se.tink.libraries.transfer.rpc.RemittanceInformation;
import se.tink.libraries.transfer.rpc.Transfer;

public class TransferTest {

    @Test
    public void shouldCreateEmptyRemittanceInformationWhenNotSet() {
        final Transfer transfer = new Transfer();

        assertNull(transfer.getRemittanceInformation().getValue());
        assertNull(transfer.getRemittanceInformation().getType());
    }

    @Test
    public void shouldCreateEmptyRemittanceInformationWhenNullRemittanceInformation() {
        final Transfer transfer = new Transfer();
        transfer.setRemittanceInformation(null);

        assertNull(transfer.getRemittanceInformation().getValue());
        assertNull(transfer.getRemittanceInformation().getType());
    }

    @Test
    public void shouldCreateRemittanceInformationWithValue() {
        final Transfer transfer = new Transfer();
        transfer.setDestinationMessage("Destination message");

        assertEquals("Destination message", transfer.getRemittanceInformation().getValue());
        assertNull(transfer.getRemittanceInformation().getType());
    }

    @Test
    public void shouldSetRemittanceInformation() {
        Transfer transfer = new Transfer();
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        transfer.setRemittanceInformation(remittanceInformation);

        assertEquals(remittanceInformation, transfer.getRemittanceInformation());
    }
}
