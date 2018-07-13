package se.tink.backend.aggregation.agents.banks.swedbank;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import se.tink.backend.core.transfer.Transfer;

public class SwedbankAgentUtilsTest {

    @Test
    public void testGetReferenceType() {
        Transfer transferOcr = createTransferMessage("37578468440200775");
        Transfer transferMessage = createTransferMessage("Test message");

        testReferenceType(transferOcr, "OCR");
        testReferenceType(transferMessage, "MESSAGE");
    }

    private void testReferenceType(Transfer transfer, String prediction) {
        String actual = SwedbankAgentUtils.getReferenceTypeFor(transfer);

        Assertions.assertThat(actual).isEqualTo(prediction);
    }

    private Transfer createTransferMessage(String destinationMessage) {
        Transfer transfer = new Transfer();
        transfer.setDestinationMessage(destinationMessage);

        return transfer;
    }

}