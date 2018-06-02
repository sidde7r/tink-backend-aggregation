package se.tink.backend.main.validators.exception;

import se.tink.backend.core.transfer.Transfer;

public class TransferValidationException extends AbstractTransferException {
    public static TransferExceptionBuilder<TransferValidationException> builder(Transfer transfer) {
        return new TransferExceptionBuilder<>(transfer, new TransferValidationException());
    }

    @Override
    public void setMessage(String message) {
        this.message = String.format("Transfer validation failed ( %s )", message);
    }
}
