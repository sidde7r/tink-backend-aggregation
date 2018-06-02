package se.tink.backend.main.validators;

import se.tink.backend.core.transfer.SignableOperationStatuses;

public class BadTransferRequestException extends RuntimeException {

    private static final long serialVersionUID = 1316042435107313079L;
    private final SignableOperationStatuses status;

    public BadTransferRequestException(String message, SignableOperationStatuses status) {
        super(message);
        this.status = status;
    }

    public SignableOperationStatuses getStatus() {
        return status;
    }
}
