package se.tink.backend.aggregation.nxgen.controllers.transfer;

import java.util.Optional;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import se.tink.backend.aggregation.agents.TransferExecutionException;
import se.tink.libraries.transfer.enums.MessageType;
import se.tink.libraries.transfer.enums.TransferType;
import se.tink.backend.core.transfer.Transfer;
import se.tink.libraries.account.AccountIdentifier;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class TransferControllerTest {
    private TransferController transferControllerWithAllExecutors;
    private PaymentExecutor paymentExecutor;
    private BankTransferExecutor bankTransferExecutor;
    private ApproveEInvoiceExecutor approveEInvoiceExecutor;
    private UpdatePaymentExecutor updatePaymentExecutor;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setup() {
        paymentExecutor = mock(PaymentExecutor.class);
        bankTransferExecutor = mock(BankTransferExecutor.class);
        approveEInvoiceExecutor = mock(ApproveEInvoiceExecutor.class);
        updatePaymentExecutor = mock(UpdatePaymentExecutor.class);
        transferControllerWithAllExecutors = new TransferController(paymentExecutor, bankTransferExecutor,
                approveEInvoiceExecutor, updatePaymentExecutor);
    }

    private Transfer createTransfer(TransferType transferType) {
        Transfer transfer = new Transfer();
        transfer.setType(transferType);
        return transfer;
    }

    private Transfer createBelgianTransfer() {
        Transfer transfer = new Transfer();
        transfer.setType(TransferType.BANK_TRANSFER);
        // Source iban is just an example that's fetched from an online website.
        transfer.setSource(AccountIdentifier.create(AccountIdentifier.Type.SEPA_EUR, "BE68539007547034"));
        return transfer;
    }

    @Test(expected = NullPointerException.class)
    public void ensureNullPointerExceptionIsThrown_whenNullIsInjectedToExecute() {
        transferControllerWithAllExecutors.execute(null);
    }

    @Test(expected = NullPointerException.class)
    public void ensureNullPointerExceptionIsThrown_whenNullIsInjectedToUpdate() {
        transferControllerWithAllExecutors.update(null);
    }

    @Test(expected = NullPointerException.class)
    public void ensureNullPointerExceptionIsThrown_whenBankTransferExecutorIsNull() {
        BankTransferExecutor transferExecutor = null;

        TransferController transferController = new TransferController(paymentExecutor, transferExecutor,
                approveEInvoiceExecutor, updatePaymentExecutor);
        transferController.execute(createTransfer(TransferType.BANK_TRANSFER));
    }

    @Test(expected = NullPointerException.class)
    public void ensureNullPointerExceptionIsThrown_whenPaymentExecutorIsNull() {
        TransferController transferController = new TransferController(null, bankTransferExecutor,
                approveEInvoiceExecutor, updatePaymentExecutor);

        transferController.execute(createTransfer(TransferType.PAYMENT));
    }

    @Test(expected = TransferExecutionException.class)
    public void ensureTransferExecutionExceptionIsThrown_whenExecuteOfNotImplementedType() {
        transferControllerWithAllExecutors.execute(createTransfer(TransferType.EINVOICE));
    }

    @Test(expected = NullPointerException.class)
    public void ensureNullPointerExceptionIsThrown_whenApproveEInvoiceExecutorIsNull() {
        TransferController transferController = new TransferController(paymentExecutor, bankTransferExecutor,
                null, updatePaymentExecutor);

        transferController.update(createTransfer(TransferType.EINVOICE));
    }

    @Test(expected = NullPointerException.class)
    public void ensureNullPointerExceptionIsThrown_whenUpdatePaymentExecutorIsNull() {
        TransferController transferController = new TransferController(paymentExecutor, bankTransferExecutor,
                approveEInvoiceExecutor, null);

        transferController.update(createTransfer(TransferType.PAYMENT));
    }

    @Test(expected = TransferExecutionException.class)
    public void ensureTransferExecutionExceptionIsThrown_whenUpdateOfNotImplementedType() {
        transferControllerWithAllExecutors.update(createTransfer(TransferType.BANK_TRANSFER));
    }

    @Test
    public void ensureTransferExecutionExceptionIsThrown_whenSourceIsBelgianAccount_andMessageTypeIsNotSet() {
        expectedException.expect(TransferExecutionException.class);
        expectedException.expectMessage("Message type have to be set for transfers of this type");

        transferControllerWithAllExecutors.execute(createBelgianTransfer());
    }

    @Test
    public void ensureSuccess_whenMessageTypeIsStructuredOgmVcs_andValid() {
        Transfer transfer = createBelgianTransfer();
        transfer.setMessageType(MessageType.STRUCTURED);
        transfer.setDestinationMessage("+++010/8068/17183+++");

        doReturn(Optional.empty()).when(bankTransferExecutor).executeTransfer(transfer);

        transferControllerWithAllExecutors.execute(transfer);
    }

    @Test
    public void ensureTransferExecutionExceptionIsThrown_whenMessageTypeIsStructuredOgmVcs_andInvalid() {
        Transfer transfer = createBelgianTransfer();
        transfer.setMessageType(MessageType.STRUCTURED);
        transfer.setDestinationMessage("+++123/4567/89101+++");

        expectedException.expect(TransferExecutionException.class);
        expectedException.expectMessage(
                TransferExecutionException.EndUserMessage.INVALID_STRUCTURED_MESSAGE.getKey().get());

        transferControllerWithAllExecutors.execute(transfer);
    }

    @Test
    public void ensureSuccess_whenMessageTypeIsFreeText() {
        Transfer transfer = createBelgianTransfer();
        transfer.setMessageType(MessageType.FREE_TEXT);

        doReturn(Optional.empty()).when(bankTransferExecutor).executeTransfer(transfer);

        transferControllerWithAllExecutors.execute(transfer);
    }
}
