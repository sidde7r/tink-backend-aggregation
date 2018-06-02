package se.tink.backend.main.validators.exception;

import com.google.api.client.repackaged.com.google.common.base.Strings;
import se.tink.backend.core.User;
import se.tink.backend.core.signableoperation.SignableOperation;
import se.tink.backend.core.transfer.SignableOperationStatuses;
import se.tink.backend.core.transfer.Transfer;
import se.tink.backend.utils.LogUtils;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.i18n.LocalizableEnum;
import se.tink.libraries.i18n.LocalizableKey;
import se.tink.libraries.i18n.LocalizableParametrizedEnum;
import se.tink.libraries.i18n.LocalizableParametrizedKey;
import se.tink.libraries.uuid.UUIDUtils;

public abstract class AbstractTransferException extends Exception {
    private Transfer transfer;
    protected String message;
    private LocalizableKey endUserMessage;
    private LocalizableParametrizedKey endUserMessageParametrized;
    private SignableOperationStatuses status = SignableOperationStatuses.CANCELLED;

    public AbstractTransferException build(Transfer transfer, LocalizableKey endUserMessage, SignableOperationStatuses status) {
        this.transfer = transfer;
        this.endUserMessage = endUserMessage;
        this.endUserMessageParametrized = null;
        this.status = status;

        return this;
    }

    public AbstractTransferException build(Transfer transfer, LocalizableParametrizedKey endUserMessageParametrized,
                                           SignableOperationStatuses status) {
        this.transfer = transfer;
        this.endUserMessage = null;
        this.endUserMessageParametrized = endUserMessageParametrized;
        this.status = status;

        return this;
    }

    public abstract void setMessage(String message);

    @Override
    public String getMessage() {
        message = !Strings.isNullOrEmpty(message) ? message : "No log message provided by the developer";

        if (transfer != null) {
            return String.format(message + " - %s", transfer);
        }

        return message;
    }

    public String getEndUserMessage(User user) {
        Catalog catalog = Catalog.getCatalog(user.getLocale());

        if (endUserMessage != null) {
            return catalog.getString(endUserMessage);
        } else if (endUserMessageParametrized != null) {
            return catalog.getString(endUserMessageParametrized);
        }

        return null;
    }

    public SignableOperation getSignableOperation(User user) {
        SignableOperation signableOperation;

        if (transfer == null) {
            signableOperation = SignableOperation.create(user, status);
        } else {
            transfer.setUserId(UUIDUtils.fromTinkUUID(user.getId()));
            signableOperation = SignableOperation.create(transfer, status);
        }

        signableOperation.setStatusMessage(getEndUserMessage(user));

        return signableOperation;
    }

    public void logDetails(final User user, LogUtils log) {
        if (transfer == null) {
            log.error(user.getId(), getMessage(), this);
            return;
        }

        if (transfer.getUserId() == null) {
            transfer.setUserId(UUIDUtils.fromTinkUUID(user.getId()));
        }

        switch(status) {
            case FAILED:
                log.error(transfer, getMessage(), this);
                break;
            case CANCELLED:
                log.warn(transfer, getMessage(), this);
                break;
            default:
                log.error(transfer, String.format("Unexpected SignableOperationStatus given ( %s )", status));
        }
    }

    public enum LogMessageParametrized {
        TO_LARGE_AMOUNT_BANK_TRANSFER("Transfer amount to large, current threshold: %s"),
        TO_LARGE_AMOUNT_PAYMENT("Payment amount to large, current threshold: %s"),
        TO_LARGE_TOTAL_AMOUNT_TRANSFERRED("Too much money have been moved for this user, total amount transferred: %s, current threshold: %s"),
        UPDATED_DESTINATION("Destination account have been modified, existing destinationAccount: %s"),
        DUPLICATE_TRANSFER("An identical transfer was executed less than %s minutes ago, aborting this one");

        private String message;

        LogMessageParametrized(String message) {
            this.message = message;
        }

        public String with(Object... parameters) {
            if (parameters != null && parameters.length > 0) {
                return String.format(message, parameters);
            }

            return message;
        }
    }

    public enum EndUserMessageParametrized implements LocalizableParametrizedEnum {
        TO_LARGE_AMOUNT_BANK_TRANSFER(new LocalizableParametrizedKey("The maximum transfer amount is {0}.")),
        TO_LARGE_AMOUNT_PAYMENT(new LocalizableParametrizedKey("The maximum payment amount is {0}.")),
        CURRENCY_NOT_AVAILABLE(new LocalizableParametrizedKey("Currency {0} not available."));

        private LocalizableParametrizedKey key;

        EndUserMessageParametrized(LocalizableParametrizedKey key) {
            this.key = key;
        }

        @Override
        public LocalizableParametrizedKey getKey() {
            return key;
        }

        public LocalizableParametrizedKey cloneWith(Object... parameters) {
            return key.cloneWith(parameters);
        }
    }

    public enum LogMessage {
        MISSING_AMOUNT("Amount were null or 0"),
        MISSING_CURRENCY("Currency were null or empty"),
        MISSING_TRANSFER_FLAG("User doesn't have privileges to make transfers"),
        MISSING_AUTHENTICATED_USER("No AuthenticatedUser provided"),
        MISSING_USER("No User provided"),
        MISSING_USER_ID("No user id provided"),
        MISSING_ACCOUNT_ID("No account id provided"),
        MISSING_TRANSFER("No Transfer provided"),
        MISSING_SOURCE("No SourceIdentifier provided"),
        MISSING_DESTINATION("No destination identifier provided"),
        MISSING_CREDENTIALS_ID("Couldn't find credentialsId on account"),
        MISSING_PAYMENT_DATE("No payment date provided"),
        MISSING_PAYMENT_DESTINATION_MESSAGE("Missing destination message on payment"),
        MISSING_EXISTING_TRANSFER("Existing transfer to compare destinations with not provided"),
        INVALID_DESTINATION("Invalid destination identifier provided"),
        INVALID_SOURCE("Invalid source identifier provided"),
        INVALID_PREFERRED_SOURCE("Invalid preferred source identifier found on account from database"),
        INVALID_AMOUNT_FORMAT("Invalid amount format"),
        INVALID_AMOUNT_UNEXPECTED("Unexpected invalid amount format, should not end up here"),
        INVALID_RUNNABLE("Could not create runnable for Transfer"),
        NOT_FOUND_ACCOUNTS("Couldn't find accounts for user in database"),
        NOT_FOUND_PATTERNS("Couldn't find transfer destination patterns for user"),
        NOT_FOUND_ACCOUNT_IDENTIFIERS("Couldn't find identifiers for account"),
        NOT_FOUND_ACCOUNT("Couldn't find destination account in database"),
        NOT_FOUND_CREDENTIALS("Account credentials not found in DB"),
        NOT_FOUND_PREFERRED_SOURCE_IDENTIFIER("Couldn't find preferred source identifier"),
        NO_MATCH_ACCOUNTS("The provided source identifier doesn't match any of the users accounts"),
        NO_MATCH_ACCOUNTS_PATTERNS("Couldn't match any destinationIdentifier for the account with users TransferDestinationPatterns"),
        NO_MATCH_IDENTIFIER_PATTERNS("Couldn't match Transfers destinationIdentifier with the users TransferDestinationPatterns"),
        NEGATIVE_AMOUNT("Amount were negative or 0"),
        UPDATED_TRANSFER_TYPE("User tried to update transfer type"),
        INCOMPATIBLE_TRANSFER_AND_DESTINATION_TYPES("Destination identifier type not compatible with transfer type"),
        PAYMENT_DATE_BEFORE_TODAY("Payment date is set before today"),
        PAYMENT_DATE_NOT_BUSINESS_DAY("Payment date is not a business day"),
        CURRENCY_NOT_AVAILABLE("Currency not available"),
        SAME_ACCOUNT_AS_SOURCE_AND_DESTINATION("Source and destination account identifiers are the same");

        private String message;

        LogMessage(String message) {
            this.message = message;
        }

        public String get() {
            return message;
        }
    }

    public enum EndUserMessage implements LocalizableEnum {
        MISSING_SOURCE(new LocalizableKey("Please choose a source account.")),
        MISSING_AMOUNT(new LocalizableKey("Please choose an amount")),
        MISSING_CURRENCY(new LocalizableKey("Please choose a currency")),
        MISSING_DESTINATION(new LocalizableKey("Please choose a destination account.")),
        MISSING_PAYMENT_DESTINATION_MESSAGE(new LocalizableKey("You need to specify a message/ocr when making a payment.")),
        MISSING_PAYMENT_DATE(new LocalizableKey("Please choose a payment date.")),
        INVALID_AMOUNT_FORMAT(new LocalizableKey("Amount cannot be empty.")),
        INVALID_DESTINATION(new LocalizableKey("Destination account is not valid from source account.")),
        INVALID_SOURCE_ACCOUNT(new LocalizableKey("Invalid source account.")),
        PAYMENT_DATE_BEFORE_TODAY(new LocalizableKey("Payment date cannot be set before today.")),
        PAYMENT_DATE_NOT_BUSINESS_DAY(new LocalizableKey("Payment date have to be a business day.")),
        UPDATE_DESTINATION(new LocalizableKey("You are not allowed to modify destination account.")),
        TO_LARGE_TOTAL_AMOUNT_TRANSFERRED(new LocalizableKey("There is a limit on how much you can transfer every 5 minutes. Please wait a while and try again.")),
        NEGATIVE_AMOUNT(new LocalizableKey("Amount has to be a positive value.")),
        DUPLICATE_TRANSFER(new LocalizableKey("You just made a transfer just like this one. Make sure it's not a duplicate.")),
        ACCESS_DENIED(new LocalizableKey("Access denied.")),
        TEMPORARY_DISABLED(new LocalizableKey("The possibility to make transfers and pay invoices has temporarily been disabled. Please try again later.")),
        VERIFY_BANK_CONNECTION(new LocalizableKey("Please verify your bank connection before making a transfer.")),
        FAILED_EXECUTE_TRANSFER(new LocalizableKey("Couldn't execute transfer.")),
        FAILED_UPDATE_TRANSFER(new LocalizableKey("Couldn't update transfer.")),
        SAME_ACCOUNT_AS_SOURCE_AND_DESTINATION(new LocalizableKey("The source and destination accounts cannot be the same."));

        private LocalizableKey key;

        EndUserMessage(LocalizableKey key) {
            this.key = key;
        }

        public LocalizableKey getKey() {
            return key;
        }
    }
}
