package se.tink.backend.aggregation.nxgen.controllers.transfer.nxgen;

import org.junit.Test;
import se.tink.backend.aggregation.agents.TransferExecutionException;
import se.tink.libraries.signableoperation.enums.SignableOperationStatuses;
import se.tink.libraries.transfer.rpc.Transfer;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.agents.TransferExecutionException.*;

public class BankTransferControllerNxgenTest {

    @Test(expected = NullPointerException.class)
    public void noExecutorShouldThrowException() {
        new BankTransferControllerNxgen(null);
    }

    @Test
    public void noSourceAccountShouldTrowException() {
        BankTransferControllerNxgen controller = new BankTransferControllerNxgen(mock(BankTransferExecutorNxgen.class));

        Transfer transfer = new Transfer();

        try {
            controller.executeTransfer(transfer);
        } catch (TransferExecutionException e) {
            assertThat(e.getSignableOperationStatus()).isEqualTo(SignableOperationStatuses.FAILED);
            assertThat(e.getUserMessage()).isEqualTo(EndUserMessage.INVALID_SOURCE.getKey().get());
        }
    }

    @Test
    public void noDestinationAccountShouldTrowException() {
        BankTransferControllerNxgen controller = new BankTransferControllerNxgen(mock(BankTransferExecutorNxgen.class));

        Transfer transfer = new Transfer();
        transfer.setSource(new SwedishIdentifier("33001212121212"));

        try {
            controller.executeTransfer(transfer);
        } catch (TransferExecutionException e) {
            assertThat(e.getSignableOperationStatus()).isEqualTo(SignableOperationStatuses.FAILED);
            assertThat(e.getUserMessage()).isEqualTo(EndUserMessage.INVALID_DESTINATION.getKey().get());
        }
    }

    /**
     * Test that it we throw a transfer exception if the user has unsigned transfers in the outbox.
     */
    @Test
    public void existingUnsignedTransfersInOutboxShouldThrowExceptions() {
        BankTransferExecutorNxgen executor = mock(BankTransferExecutorNxgen.class);

        when(executor.isOutboxEmpty()).thenReturn(false);

        BankTransferControllerNxgen controller = new BankTransferControllerNxgen(executor);

        Transfer transfer = new Transfer();
        transfer.setSource(new SwedishIdentifier("33001212121212"));
        transfer.setDestination(new SwedishIdentifier("33001212121213"));

        try {
            controller.executeTransfer(transfer);
        } catch (TransferExecutionException e) {
            assertThat(e.getSignableOperationStatus()).isEqualTo(SignableOperationStatuses.CANCELLED);
            assertThat(e.getUserMessage()).isEqualTo(EndUserMessage.EXISTING_UNSIGNED_TRANSFERS.getKey().get());
        }
    }
}
