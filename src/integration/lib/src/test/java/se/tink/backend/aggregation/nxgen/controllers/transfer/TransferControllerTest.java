package se.tink.backend.aggregation.nxgen.controllers.transfer;

import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import se.tink.backend.aggregation.agents.exceptions.transfer.TransferExecutionException;
import se.tink.libraries.transfer.enums.TransferType;
import se.tink.libraries.transfer.rpc.Transfer;

public class TransferControllerTest {
    private TransferController transferControllerWithAllExecutors;
    private PaymentExecutor paymentExecutor;
    private BankTransferExecutor bankTransferExecutor;
    private ApproveEInvoiceExecutor approveEInvoiceExecutor;
    private UpdatePaymentExecutor updatePaymentExecutor;

    @Rule public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setup() {
        paymentExecutor = mock(PaymentExecutor.class);
        bankTransferExecutor = mock(BankTransferExecutor.class);
        approveEInvoiceExecutor = mock(ApproveEInvoiceExecutor.class);
        updatePaymentExecutor = mock(UpdatePaymentExecutor.class);
        transferControllerWithAllExecutors =
                new TransferController(
                        paymentExecutor,
                        bankTransferExecutor,
                        approveEInvoiceExecutor,
                        updatePaymentExecutor);
    }

    private Transfer createTransfer(TransferType transferType) {
        Transfer transfer = new Transfer();
        transfer.setType(transferType);
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

        TransferController transferController =
                new TransferController(
                        paymentExecutor,
                        transferExecutor,
                        approveEInvoiceExecutor,
                        updatePaymentExecutor);
        transferController.execute(createTransfer(TransferType.BANK_TRANSFER));
    }

    @Test(expected = NullPointerException.class)
    public void ensureNullPointerExceptionIsThrown_whenPaymentExecutorIsNull() {
        TransferController transferController =
                new TransferController(
                        null, bankTransferExecutor, approveEInvoiceExecutor, updatePaymentExecutor);

        transferController.execute(createTransfer(TransferType.PAYMENT));
    }

    @Test(expected = TransferExecutionException.class)
    public void ensureTransferExecutionExceptionIsThrown_whenExecuteOfNotImplementedType() {
        transferControllerWithAllExecutors.execute(createTransfer(TransferType.EINVOICE));
    }

    @Test(expected = NullPointerException.class)
    public void ensureNullPointerExceptionIsThrown_whenApproveEInvoiceExecutorIsNull() {
        TransferController transferController =
                new TransferController(
                        paymentExecutor, bankTransferExecutor, null, updatePaymentExecutor);

        transferController.update(createTransfer(TransferType.EINVOICE));
    }

    @Test(expected = NullPointerException.class)
    public void ensureNullPointerExceptionIsThrown_whenUpdatePaymentExecutorIsNull() {
        TransferController transferController =
                new TransferController(
                        paymentExecutor, bankTransferExecutor, approveEInvoiceExecutor, null);

        transferController.update(createTransfer(TransferType.PAYMENT));
    }

    @Test(expected = TransferExecutionException.class)
    public void ensureTransferExecutionExceptionIsThrown_whenUpdateOfNotImplementedType() {
        transferControllerWithAllExecutors.update(createTransfer(TransferType.BANK_TRANSFER));
    }
}
