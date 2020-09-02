package se.tink.backend.aggregation.utils.transfer;

import se.tink.backend.aggregation.agents.exceptions.transfer.TransferExecutionException;
import se.tink.libraries.signableoperation.enums.SignableOperationStatuses;

public class TransferMessageException extends TransferExecutionException {
    public TransferMessageException(String userMessage, String logMessage) {
        super(logMessage);
        this.setSignableOperationStatus(SignableOperationStatuses.CANCELLED);
        this.setUserMessage(userMessage);
    }

    public TransferMessageException(String userMessage, String logMessage, String internalStatus) {
        super(logMessage);
        this.setSignableOperationStatus(SignableOperationStatuses.CANCELLED);
        this.setUserMessage(userMessage);
        this.setInternalStatus(internalStatus);
    }
}
