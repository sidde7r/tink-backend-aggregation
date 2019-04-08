package se.tink.backend.aggregation.nxgen.controllers.transfer;

import com.google.common.base.Preconditions;
import java.util.Optional;
import se.tink.backend.aggregation.agents.TransferExecutionException;
import se.tink.backend.aggregation.nxgen.controllers.transfer.nxgen.BankTransferControllerNxgen;
import se.tink.backend.aggregation.nxgen.controllers.transfer.nxgen.BankTransferExecutorNxgen;
import se.tink.backend.aggregation.nxgen.controllers.transfer.validators.StructuredMessageValidator;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.signableoperation.enums.SignableOperationStatuses;
import se.tink.libraries.transfer.enums.MessageType;
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

    public TransferController(
            PaymentExecutor paymentExecutor,
            BankTransferExecutorNxgen bankTransferExecutor,
            ApproveEInvoiceExecutor approveEInvoiceExecutor,
            UpdatePaymentExecutor updatePaymentExecutor) {
        this.paymentExecutor = paymentExecutor;
        this.bankTransferExecutor = new BankTransferControllerNxgen(bankTransferExecutor);
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

        switch (transfer.getType()) {
            case EINVOICE:
                approveEInvoice(transfer);
                break;
            case PAYMENT:
                updatePayment(transfer);
                break;
            default:
                TransferExecutionException.throwIf(true);
        }
    }

    private Optional<String> executeBankTransfer(final Transfer transfer) {
        Preconditions.checkNotNull(bankTransferExecutor);

        if (transfer.getSource().is(AccountIdentifier.Type.BE)
                || transfer.getSource().is(AccountIdentifier.Type.SEPA_EUR)) {
            validateTransferMessageType(transfer);
        }

        return bankTransferExecutor.executeTransfer(transfer);
    }

    private void executePayment(final Transfer transfer) {
        Preconditions.checkNotNull(paymentExecutor);

        paymentExecutor.executePayment(transfer);
    }

    private void approveEInvoice(final Transfer transfer) {
        Preconditions.checkNotNull(approveEInvoiceExecutor);

        approveEInvoiceExecutor.approveEInvoice(transfer);
    }

    private void updatePayment(final Transfer transfer) {
        Preconditions.checkNotNull(updatePaymentExecutor);

        updatePaymentExecutor.updatePayment(transfer);
    }

    private void validateTransferMessageType(Transfer transfer) {
        if (transfer.getMessageType() == null) {
            throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                    .setEndUserMessage(
                            TransferExecutionException.EndUserMessage.MISSING_MESSAGE_TYPE
                                    .getKey()
                                    .get())
                    .setMessage("Message type have to be set for transfers of this type")
                    .build();
        }

        if (transfer.getMessageType() == MessageType.STRUCTURED
                && !StructuredMessageValidator.isValidOgmVcs(transfer.getDestinationMessage())) {
            String errorMessage =
                    TransferExecutionException.EndUserMessage.INVALID_STRUCTURED_MESSAGE
                            .getKey()
                            .get();
            throw TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                    .setEndUserMessage(errorMessage)
                    .setMessage(errorMessage)
                    .build();
        }
    }
}
