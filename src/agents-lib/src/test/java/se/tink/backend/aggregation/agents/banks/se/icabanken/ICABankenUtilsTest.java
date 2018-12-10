package se.tink.backend.aggregation.agents.banks.se.icabanken;

import org.junit.Test;
import se.tink.libraries.helper.transfer.stubs.TransferStub;
import se.tink.backend.core.transfer.Transfer;

import static org.assertj.core.api.Assertions.assertThat;

public class ICABankenUtilsTest {

    @Test
    public void testGetTransferTypeFromDestinationMessage() {
        Transfer ocr = createTransfer("38173926046183528");
        Transfer message = createTransfer("test message");

        testGetTransferType(ocr, "Ocr");
        testGetTransferType(message, "Message");
    }

    private void testGetTransferType(Transfer transfer, String prediction) {
        String referenceType = ICABankenUtils.getReferenceTypeFor(transfer);

        assertThat(referenceType).isEqualTo(prediction);
    }

    private Transfer createTransfer(String destinationMessage) {
        Transfer transfer = new Transfer();
        transfer.setDestinationMessage(destinationMessage);

        return transfer;
    }

    @Test
    public void findOrCreateDueDate_shouldNeverBeNull() {
        Transfer transfer = TransferStub
                .bankTransfer()
                .withDueDate(null)
                .build();

        String dueDate = ICABankenUtils.findOrCreateDueDateFor(transfer);
        assertThat(dueDate).isNotNull().isNotEmpty();
    }
}