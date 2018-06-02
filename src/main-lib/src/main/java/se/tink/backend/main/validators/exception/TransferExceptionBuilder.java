package se.tink.backend.main.validators.exception;

import se.tink.libraries.i18n.LocalizableKey;
import se.tink.libraries.i18n.LocalizableParametrizedKey;
import se.tink.backend.core.transfer.SignableOperationStatuses;
import se.tink.backend.core.transfer.Transfer;
import se.tink.backend.main.validators.exception.AbstractTransferException.EndUserMessage;
import se.tink.backend.main.validators.exception.AbstractTransferException.LogMessage;

public class TransferExceptionBuilder<T extends AbstractTransferException> {
    private final T exception;
    private final Transfer transfer;
    private LocalizableKey endUserMessage;
    private LocalizableParametrizedKey endUserMessageParametrized;
    private String logMessage = "Reason unknown";

    TransferExceptionBuilder(T exception) {
        this(null, exception);
    }

    TransferExceptionBuilder(Transfer transfer, T exception) {
        this.transfer = transfer;
        this.exception = exception;
    }

    public TransferExceptionBuilder<T> setEndUserMessage(EndUserMessage endUserMessage) {
        if (endUserMessage != null) {
            this.endUserMessage = endUserMessage.getKey();
            this.endUserMessageParametrized = null;
        }
        return this;
    }

    public TransferExceptionBuilder<T> setEndUserMessage(LocalizableParametrizedKey endUserMessageParametrized) {
        if (endUserMessageParametrized != null) {
            this.endUserMessageParametrized = endUserMessageParametrized;
            this.endUserMessage = null;
        }
        return this;
    }

    public TransferExceptionBuilder<T> setLogMessage(String logMessage) {
        this.logMessage = logMessage;
        return this;
    }

    public TransferExceptionBuilder<T> setLogMessage(LogMessage logMessage) {
        this.logMessage = logMessage.get();
        return this;
    }

    private String getLogMessage() {
        return logMessage;
    }

    public T build(SignableOperationStatuses status) {
        if (endUserMessageParametrized != null) {
            exception.build(transfer, endUserMessageParametrized, status);
        } else {
            exception.build(transfer, endUserMessage, status);
        }

        exception.setMessage(getLogMessage());

        return exception;
    }

    public T build() {
        return build(SignableOperationStatuses.CANCELLED);
    }
}
