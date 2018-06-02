package se.tink.backend.main.validators.exception;

import se.tink.backend.core.transfer.Transfer;

public class TransferEnricherException extends AbstractTransferException {
    public static TransferExceptionBuilder<TransferEnricherException> builder(Transfer transfer) {
        return new TransferExceptionBuilder<>(transfer, new TransferEnricherException());
    }

    @Override
    public void setMessage(String message) {
        this.message = String.format("Failed to enrich transfer ( %s )", message);
    }
}
