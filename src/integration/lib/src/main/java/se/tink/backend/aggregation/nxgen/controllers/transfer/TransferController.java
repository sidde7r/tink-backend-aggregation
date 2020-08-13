package se.tink.backend.aggregation.nxgen.controllers.transfer;

import com.google.common.base.Preconditions;
import java.util.Optional;
import se.tink.backend.aggregation.agents.exceptions.transfer.TransferExecutionException;
import se.tink.libraries.transfer.enums.TransferType;
import se.tink.libraries.transfer.rpc.Transfer;

public class TransferController {
    private final PaymentExecutor paymentExecutor;
    private final BankTransferExecutor bankTransferExecutor;
    private final ApproveEInvoiceExecutor approveEInvoiceExecutor;
    private final UpdatePaymentExecutor updatePaymentExecutor;

    public TransferController(
            PaymentExecutor paymentExecutor,
            BankTransferExecutor bankTransferExecutor,
            ApproveEInvoiceExecutor approveEInvoiceExecutor,
            UpdatePaymentExecutor updatePaymentExecutor) {
        this.paymentExecutor = paymentExecutor;
        this.bankTransferExecutor = bankTransferExecutor;
        this.approveEInvoiceExecutor = approveEInvoiceExecutor;
        this.updatePaymentExecutor = updatePaymentExecutor;
    }

    public Optional<String> execute(final Transfer transfer) {
        Preconditions.checkNotNull(transfer);

        switch (transfer.getType()) {
            case BANK_TRANSFER:
                return executeBankTransfer(transfer);
            case PAYMENT:
                executePayment(transfer);
                break;
            default:
                TransferExecutionException.throwIf(true);
        }
        return Optional.empty();
    }

    public void update(Transfer transfer) {
        Preconditions.checkNotNull(transfer);

        if (transfer.getType() == TransferType.PAYMENT) {
            updatePayment(transfer);
        } else {
            TransferExecutionException.throwIf(true);
        }
    }

    private Optional<String> executeBankTransfer(final Transfer transfer) {
        Preconditions.checkNotNull(bankTransferExecutor);

        return bankTransferExecutor.executeTransfer(transfer);
    }

    private void executePayment(final Transfer transfer) {
        Preconditions.checkNotNull(paymentExecutor);

        paymentExecutor.executePayment(transfer);
    }
}
