package se.tink.backend.aggregation.agents.banks.sbab.executor.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.banks.sbab.executor.entities.ErrorEntity;
import se.tink.backend.aggregation.agents.exceptions.transfer.TransferExecutionException;
import se.tink.backend.aggregation.agents.exceptions.transfer.TransferExecutionException.EndUserMessage;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.signableoperation.enums.SignableOperationStatuses;

@JsonObject
public class ErrorResponse {
    private static final Logger log = LoggerFactory.getLogger(TransferResponse.class);

    private List<ErrorEntity> errors;

    @JsonIgnore
    public ErrorEntity getError() {
        if (errors == null || errors.isEmpty()) {
            log.warn("Transfer failed with no error present");
            return null;
        }

        // Haven't seen more than one error even though it's a list, best guess is to select first.
        return errors.get(0);
    }

    @JsonIgnore
    public void handleTransferValidationErrors(Catalog catalog) {
        ErrorEntity error = getError();

        if (error == null) {
            return;
        }

        if (error.isExcessTransferAmount()) {
            throw TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                    .setMessage(catalog.getString(EndUserMessage.EXCESS_AMOUNT))
                    .setEndUserMessage(catalog.getString(EndUserMessage.EXCESS_AMOUNT))
                    .build();
        }

        if (error.isInvalidTransactionDate()) {
            throw TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                    .setMessage(catalog.getString(EndUserMessage.INVALID_DUEDATE_NOT_BUSINESSDAY))
                    .setEndUserMessage(
                            catalog.getString(EndUserMessage.INVALID_DUEDATE_NOT_BUSINESSDAY))
                    .build();
        }
    }

    @JsonIgnore
    public void handleTransferConfirmationErrors(Catalog catalog) {
        ErrorEntity error = getError();

        if (error != null && error.isInvalidAccountNumberLength()) {
            throw TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                    .setMessage(catalog.getString(EndUserMessage.INVALID_DESTINATION))
                    .setEndUserMessage(catalog.getString(EndUserMessage.INVALID_DESTINATION))
                    .build();
        }
    }

    @JsonIgnore
    public void handleRecipientValidationErrors(Catalog catalog) {
        ErrorEntity error = getError();

        if (error != null && error.isInvalidAccountNumber()) {
            throw TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                    .setMessage(catalog.getString(EndUserMessage.INVALID_DESTINATION))
                    .setEndUserMessage(catalog.getString(EndUserMessage.INVALID_DESTINATION))
                    .build();
        }
    }
}
