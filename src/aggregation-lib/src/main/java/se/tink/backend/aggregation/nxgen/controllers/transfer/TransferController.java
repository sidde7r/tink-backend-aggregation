package se.tink.backend.aggregation.nxgen.controllers.transfer;

import com.google.common.base.Preconditions;
import se.tink.backend.aggregation.agents.TransferExecutionException;
import se.tink.backend.aggregation.nxgen.controllers.transfer.validators.StructuredMessageValidator;
import se.tink.backend.core.enums.MessageType;
import se.tink.backend.core.transfer.SignableOperationStatuses;
import se.tink.backend.core.transfer.Transfer;
import se.tink.libraries.account.AccountIdentifier;

public class TransferController {
    private final PaymentExecutor paymentExecutor;
    private final BankTransferExecutor bankTransferExecutor;
    private final ApproveEInvoiceExecutor approveEInvoiceExecutor;
    private final UpdatePaymentExecutor updatePaymentExecutor;

    public TransferController(PaymentExecutor paymentExecutor, BankTransferExecutor bankTransferExecutor,
            ApproveEInvoiceExecutor approveEInvoiceExecutor, UpdatePaymentExecutor updatePaymentExecutor) {
        this.paymentExecutor = paymentExecutor;
        this.bankTransferExecutor = bankTransferExecutor;
        this.approveEInvoiceExecutor = approveEInvoiceExecutor;
        this.updatePaymentExecutor = updatePaymentExecutor;
    }

    public void execute(final Transfer transfer) {
        Preconditions.checkNotNull(transfer);

        switch (transfer.getType()) {
        case BANK_TRANSFER:
            executeBankTransfer(transfer);
            break;
        case PAYMENT:
            executePayment(transfer);
            break;
        default:
            TransferExecutionException.throwIf(true);
        }
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

    private void executeBankTransfer(final Transfer transfer) {
        Preconditions.checkNotNull(bankTransferExecutor);

        if (transfer.getSource().is(AccountIdentifier.Type.BE)) {
            validateTransferMessageType(transfer);
        }

        bankTransferExecutor.executeTransfer(transfer);
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
                    .setEndUserMessage(TransferExecutionException.EndUserMessage.MISSING_MESSAGE_TYPE.getKey().get())
                    .setMessage("Message type have to be set for transfers of this type")
                    .build();
        }

        if (transfer.getMessageType() == MessageType.STRUCTURED &&
                !StructuredMessageValidator.isValidOgmVcs(transfer.getDestinationMessage())) {
            String errorMessage = TransferExecutionException.EndUserMessage.INVALID_STRUCTURED_MESSAGE.getKey().get();
            throw TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                    .setEndUserMessage(errorMessage)
                    .setMessage(errorMessage)
                    .build();
        }
    }
}
