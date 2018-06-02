package se.tink.backend.main.validators;

import com.google.common.base.Objects;
import com.google.inject.Inject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import se.tink.backend.common.config.TransfersConfiguration;
import se.tink.backend.core.enums.TransferType;
import se.tink.backend.core.transfer.SignableOperationStatuses;
import se.tink.backend.core.transfer.Transfer;
import se.tink.backend.main.validators.exception.InstantiationException;
import se.tink.backend.main.validators.exception.TransferValidationException;
import static se.tink.backend.main.validators.exception.AbstractTransferException.EndUserMessage;
import static se.tink.backend.main.validators.exception.AbstractTransferException.LogMessage;
import static se.tink.backend.main.validators.exception.AbstractTransferException.LogMessageParametrized;

public class TransferUpdateRequestValidator extends TransferRequestValidator {
    @Inject
    public TransferUpdateRequestValidator(TransfersConfiguration transferConfiguration) throws InstantiationException {
        super(transferConfiguration);
    }

    public void validateUpdates(Transfer incomingTransfer, Transfer existingTransfer) throws TransferValidationException{
        if (incomingTransfer == null) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
        if (existingTransfer == null) {
            throw TransferValidationException.builder(incomingTransfer)
                    .setLogMessage(LogMessage.MISSING_EXISTING_TRANSFER)
                    .setEndUserMessage(EndUserMessage.FAILED_UPDATE_TRANSFER)
                    .build(SignableOperationStatuses.FAILED);
        }

        validateDestinationUpdate(incomingTransfer, existingTransfer);
        validateUpdatedTransferType(incomingTransfer, existingTransfer);
    }

    private void validateDestinationUpdate(Transfer incomingTransfer, Transfer existingTransfer) throws TransferValidationException {
        if (existingTransfer.isOneOfTypes(TransferType.EINVOICE, TransferType.PAYMENT) &&
                !Objects.equal(existingTransfer.getDestination(), incomingTransfer.getDestination())) {

            throw TransferValidationException.builder(incomingTransfer)
                    .setLogMessage(LogMessageParametrized.UPDATED_DESTINATION
                            .with(existingTransfer.getDestination()))
                    .setEndUserMessage(EndUserMessage.UPDATE_DESTINATION)
                    .build();
        }
    }

    private void validateUpdatedTransferType(Transfer incomingTransfer, Transfer existingTransfer) throws TransferValidationException {
        if (!incomingTransfer.isOfType(existingTransfer.getType())) {
            throw TransferValidationException.builder(incomingTransfer)
                    .setLogMessage(LogMessage.UPDATED_TRANSFER_TYPE)
                    .setEndUserMessage(EndUserMessage.FAILED_UPDATE_TRANSFER)
                    .build(SignableOperationStatuses.FAILED);
        }
    }
}
