package se.tink.backend.aggregation.nxgen.controllers.transfer;

import com.google.common.base.Preconditions;
import java.util.Optional;
import se.tink.backend.aggregation.agents.exceptions.transfer.TransferExecutionException;
import se.tink.libraries.transfer.rpc.Transfer;

public class TransferController {
    private final PaymentExecutor paymentExecutor;
    private final BankTransferExecutor bankTransferExecutor;

    public TransferController(
            PaymentExecutor paymentExecutor, BankTransferExecutor bankTransferExecutor) {
        this.paymentExecutor = paymentExecutor;
        this.bankTransferExecutor = bankTransferExecutor;
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

    private Optional<String> executeBankTransfer(final Transfer transfer) {
        Preconditions.checkNotNull(bankTransferExecutor);

        return bankTransferExecutor.executeTransfer(transfer);
    }

    private void executePayment(final Transfer transfer) {
        Preconditions.checkNotNull(paymentExecutor);

        paymentExecutor.executePayment(transfer);
    }
}
