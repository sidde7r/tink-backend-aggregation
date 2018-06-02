package se.tink.backend.main.validators.exception;

public class TransfersTemporaryDisabledException extends AbstractTransferException {

    private static final long serialVersionUID = -5769148209930099281L;

    public static TransferExceptionBuilder<TransfersTemporaryDisabledException> builder() {
        return new TransferExceptionBuilder<>(new TransfersTemporaryDisabledException());
    }

    @Override
    public void setMessage(String message) {
        this.message = "Transfer service is temporarily disabled";
    }
}
