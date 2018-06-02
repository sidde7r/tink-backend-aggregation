package se.tink.backend.main.validators;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import java.util.Date;
import se.tink.backend.common.config.TransfersConfiguration;
import se.tink.backend.core.Amount;
import se.tink.backend.core.enums.TransferType;
import se.tink.backend.core.transfer.SignableOperationStatuses;
import se.tink.backend.core.transfer.Transfer;
import se.tink.backend.main.validators.exception.InstantiationException;
import se.tink.backend.main.validators.exception.TransferNotFoundException;
import se.tink.backend.main.validators.exception.TransferValidationException;
import se.tink.backend.main.validators.exception.TransfersTemporaryDisabledException;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.date.DateUtils;
import static se.tink.backend.main.validators.exception.AbstractTransferException.EndUserMessage;
import static se.tink.backend.main.validators.exception.AbstractTransferException.LogMessage;

public class TransferRequestValidator {
    private final TransfersConfiguration transferConfiguration;

    @Inject
    public TransferRequestValidator(TransfersConfiguration transferConfiguration) throws InstantiationException {
        if (transferConfiguration == null) {
            throw new InstantiationException(this, "No TransfersConfiguration provided");
        }

        this.transferConfiguration = transferConfiguration;
    }

    public void validateEnabled() throws TransfersTemporaryDisabledException {
        if (!transferConfiguration.isEnabled()) {
            throw TransfersTemporaryDisabledException.builder()
                    .setEndUserMessage(EndUserMessage.TEMPORARY_DISABLED)
                    .build(SignableOperationStatuses.FAILED);
        }
    }

    public void validate(Transfer transfer)
            throws TransferNotFoundException, TransfersTemporaryDisabledException, TransferValidationException {
        validateEnabled();

        if (transfer == null) {
            throw new TransferNotFoundException();
        }

        validateAmount(transfer);
        validateDestination(transfer);
        validateSource(transfer);
        validateDueDate(transfer);
        validateDestinationMessage(transfer);
    }

    private void validateAmount(Transfer transfer) throws TransferValidationException {
        Amount amount = transfer.getAmount();

        if (amount.getValue() == null || amount.getValue() == 0) {
            throw TransferValidationException.builder(transfer)
                    .setLogMessage(LogMessage.MISSING_AMOUNT)
                    .setEndUserMessage(EndUserMessage.MISSING_AMOUNT)
                    .build();

        } else if (Strings.isNullOrEmpty(amount.getCurrency())) {
            throw TransferValidationException.builder(transfer)
                    .setLogMessage(LogMessage.MISSING_CURRENCY)
                    .setEndUserMessage(EndUserMessage.MISSING_CURRENCY)
                    .build();
        } else if (!amount.isPositive()) {
            throw TransferValidationException.builder(transfer)
                    .setLogMessage(LogMessage.NEGATIVE_AMOUNT)
                    .setEndUserMessage(EndUserMessage.NEGATIVE_AMOUNT)
                    .build();
        }
    }

    private void validateDestination(Transfer transfer) throws TransferValidationException {
        final AccountIdentifier destination = transfer.getDestination();

        if (destination == null) {
            throw TransferValidationException.builder(transfer)
                    .setLogMessage(LogMessage.MISSING_DESTINATION)
                    .setEndUserMessage(EndUserMessage.MISSING_DESTINATION)
                    .build();

        } else if (!destination.isValid()) {
            throw TransferValidationException.builder(transfer)
                    .setLogMessage(LogMessage.INVALID_DESTINATION)
                    .setEndUserMessage(EndUserMessage.MISSING_DESTINATION)
                    .build();
        }

        if (transfer.getType() != null &&
                !TransferType.accountIdentifierIsCompatibleWith(destination, transfer.getType())) {
            throw TransferValidationException.builder(transfer)
                    .setLogMessage(LogMessage.INCOMPATIBLE_TRANSFER_AND_DESTINATION_TYPES)
                    .setEndUserMessage(EndUserMessage.FAILED_UPDATE_TRANSFER)
                    .build();
        }
    }

    private void validateSource(Transfer transfer) throws TransferValidationException {
        final AccountIdentifier source = transfer.getSource();

        if (source == null) {
            throw TransferValidationException.builder(transfer)
                    .setLogMessage(LogMessage.MISSING_SOURCE)
                    .setEndUserMessage(EndUserMessage.MISSING_SOURCE)
                    .build();

        } else if (!source.isValid()) {
            throw TransferValidationException.builder(transfer)
                    .setLogMessage(LogMessage.INVALID_SOURCE)
                    .setEndUserMessage(EndUserMessage.MISSING_SOURCE)
                    .build();
        }
    }

    private void validateDueDate(Transfer transfer) throws TransferValidationException {
        final Date dueDate = transfer.getDueDate();

        if (transfer.isOneOfTypes(TransferType.EINVOICE)) {
            if (dueDate == null) {
                throw TransferValidationException.builder(transfer)
                        .setLogMessage(LogMessage.MISSING_PAYMENT_DATE)
                        .setEndUserMessage(EndUserMessage.MISSING_PAYMENT_DATE)
                        .build();
            }

            if (!DateUtils.isBusinessDay(dueDate)) {
                throw TransferValidationException.builder(transfer)
                        .setLogMessage(LogMessage.PAYMENT_DATE_NOT_BUSINESS_DAY)
                        .setEndUserMessage(EndUserMessage.PAYMENT_DATE_NOT_BUSINESS_DAY)
                        .build();
            }
        } else if (transfer.isOneOfTypes(TransferType.PAYMENT)) {
            if (dueDate != null && !DateUtils.isBusinessDay(dueDate)) {
                throw TransferValidationException.builder(transfer)
                        .setLogMessage(LogMessage.PAYMENT_DATE_NOT_BUSINESS_DAY)
                        .setEndUserMessage(EndUserMessage.PAYMENT_DATE_NOT_BUSINESS_DAY)
                        .build();
            }
        }

        if (dueDate != null) {
            if (DateUtils.isBeforeToday(dueDate)) {
                throw TransferValidationException.builder(transfer)
                        .setLogMessage(LogMessage.PAYMENT_DATE_BEFORE_TODAY)
                        .setEndUserMessage(EndUserMessage.PAYMENT_DATE_BEFORE_TODAY)
                        .build(SignableOperationStatuses.FAILED);
            }
        }
    }

    private void validateDestinationMessage(Transfer transfer) throws TransferValidationException {
        if (transfer.isOneOfTypes(TransferType.EINVOICE, TransferType.PAYMENT)) {
            if (Strings.isNullOrEmpty(transfer.getDestinationMessage())) {
                throw TransferValidationException.builder(transfer)
                        .setLogMessage(LogMessage.MISSING_PAYMENT_DESTINATION_MESSAGE)
                        .setEndUserMessage(EndUserMessage.MISSING_PAYMENT_DESTINATION_MESSAGE)
                        .build();
            }
        }
    }
}
