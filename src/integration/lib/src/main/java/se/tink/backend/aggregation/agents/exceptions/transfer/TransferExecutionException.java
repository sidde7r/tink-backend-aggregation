package se.tink.backend.aggregation.agents.exceptions.transfer;

import com.google.common.base.Preconditions;
import se.tink.libraries.i18n_aggregation.LocalizableEnum;
import se.tink.libraries.i18n_aggregation.LocalizableKey;
import se.tink.libraries.i18n_aggregation.LocalizableParametrizedEnum;
import se.tink.libraries.i18n_aggregation.LocalizableParametrizedKey;
import se.tink.libraries.signableoperation.enums.SignableOperationStatuses;

/**
 * An exception thrown that bundles an friendly error message that can be presented to the end-user.
 */
public class TransferExecutionException extends RuntimeException {

    public static class Builder {

        private String endUserMessage;
        private Throwable exception;
        private String message;
        private SignableOperationStatuses status;
        private String internalStatus;

        private Builder(SignableOperationStatuses status) {
            this.status = status;
        }

        public TransferExecutionException build() {
            TransferExecutionException e;
            if (message != null && exception != null) {
                e = new TransferExecutionException(message, exception);
            } else if (message != null) {
                e = new TransferExecutionException(message);
            } else if (exception != null) {
                e = new TransferExecutionException(exception);
            } else {
                e = new TransferExecutionException();
            }
            e.setSignableOperationStatus(status);
            if (endUserMessage != null) {
                e.setUserMessage(endUserMessage);
            }
            if (internalStatus != null) {
                e.setInternalStatus(internalStatus);
            }
            return e;
        }

        public Builder setEndUserMessage(String endUserMessage) {
            this.endUserMessage = Preconditions.checkNotNull(endUserMessage);
            return this;
        }

        public Builder setEndUserMessage(EndUserMessage endUserMessage) {
            EndUserMessage message = Preconditions.checkNotNull(endUserMessage);
            this.endUserMessage = Preconditions.checkNotNull(message.getKey().get());
            return this;
        }

        public Builder setException(Throwable exception) {
            this.exception = exception;
            return this;
        }

        public Builder setMessage(String message) {
            this.message = message;
            return this;
        }

        public Builder setInternalStatus(String internalStatus) {
            this.internalStatus = internalStatus;
            return this;
        }
    }

    private static final long serialVersionUID = 3654108329798528461L;

    public static Builder builder(SignableOperationStatuses status) {
        return new Builder(status);
    }

    private String endUserMessage;
    private SignableOperationStatuses status;
    private String internalStatus;

    protected TransferExecutionException() {
        super();
    }

    protected TransferExecutionException(String message) {
        super(message);
    }

    private TransferExecutionException(String message, Throwable e) {
        super(message, e);
    }

    private TransferExecutionException(Throwable exception) {
        super(exception);
    }

    public String getUserMessage() {
        return endUserMessage;
    }

    public SignableOperationStatuses getSignableOperationStatus() {
        return status;
    }

    protected void setSignableOperationStatus(SignableOperationStatuses status) {
        this.status = status;
    }

    protected void setUserMessage(String userMessage) {
        this.endUserMessage = userMessage;
    }

    protected void setInternalStatus(String internalStatus) {
        this.internalStatus = internalStatus;
    }

    public String getInternalStatus() {
        return internalStatus;
    }

    /**
     * This is a place where we can add shared common strings that we use (or can use) for transfer
     * errors
     */
    public enum EndUserMessage implements LocalizableEnum {
        BANKID_NO_RESPONSE(
                new LocalizableKey("No response from Mobile BankID. Have you opened the app?")),
        BANKID_ANOTHER_IN_PROGRESS(
                new LocalizableKey(
                        "You have another BankID session in progress. Please try again.")),
        BANKID_CANCELLED(new LocalizableKey("You cancelled the BankID process. Please try again.")),
        BANKID_FAILED(new LocalizableKey("The BankID authentication failed")),
        BANKID_TRANSFER_FAILED(new LocalizableKey("Failed to sign transfer with BankID")),
        SIGN_TRANSFER_FAILED(new LocalizableKey("Failed to sign transfer")),
        SIGN_AND_REMOVAL_FAILED(
                new LocalizableKey(
                        "We encountered problems signing the payment/transfer with your bank. Please log in to your bank app and validate the payment/transfer.")),
        CHALLENGE_NO_RESPONSE(
                new LocalizableKey(
                        "Transfer or payment was not signed with security token device")),
        EINVOICE_MODIFY_FAILED(new LocalizableKey("Not able to update this e-invoice")),
        EINVOICE_VALIDATE_FAILED(new LocalizableKey("Could not validate e-invoice")),
        EINVOICE_NO_MATCHES(new LocalizableKey("The e-invoice could not be found at your bank")),
        EINVOICE_SIGN_FAILED(new LocalizableKey("Could not sign the e-invoice")),
        EINVOICE_MULTIPLE_MATCHES(
                new LocalizableKey(
                        "Found more than one eInvoices that exactly matches the transfer")),
        EXISTING_UNSIGNED_TRANSFERS(
                new LocalizableKey(
                        "You have existing unsigned transfers, please sign these in your bank's app before executing a new transfer")),
        DUPLICATE_PAYMENT(
                new LocalizableKey(
                        "The payment could not be made because an identical payment is already registered")),
        EXCESS_AMOUNT(
                new LocalizableKey(
                        "The transfer amount is larger than what is available on the account.")),
        INVALID_MINIMUM_AMOUNT(
                new LocalizableKey("The transfer amount, less than 1 SEK is not supported.")),
        EXCESS_AMOUNT_FOR_BENEFICIARY(
                new LocalizableKey(
                        "The transfer amount will exceed the total allowed weekly limit for the beneficiary.")),
        EXCESS_AMOUNT_AWAITING_PROCESSING(
                new LocalizableKey(
                        "The transfer has been sent to your bank and will be executed when funds are available")),
        INVALID_DESTINATION(new LocalizableKey("Invalid destination account")),
        DESTINATION_CANT_BE_SAME_AS_SOURCE(
                new LocalizableKey("Source and destination account must not be the same.")),
        INVALID_DUEDATE_NOT_BUSINESSDAY(
                new LocalizableKey("The payment date is not a business day")),
        INVALID_DUEDATE_TOO_SOON_OR_NOT_BUSINESSDAY(
                new LocalizableKey("The payment date is too soon or not a business day")),
        INVALID_DESTINATION_MESSAGE(new LocalizableKey("The destination message is not valid")),
        INVALID_OCR(new LocalizableKey("The destination message is not a valid OCR reference")),
        INVALID_MESSAGE(new LocalizableKey("The message given is not valid")),
        INVALID_SOURCE(new LocalizableKey("Invalid source account")),
        INVALID_SOURCE_NO_ENTITIES(new LocalizableKey("Could not retrieve source accounts")),
        SOURCE_NOT_FOUND(new LocalizableKey("Could not find source account")),
        NEW_RECIPIENT_FAILED(new LocalizableKey("Unable to create new recipient account")),
        NEW_RECIPIENT_NAME_ABSENT(new LocalizableKey("You must specify a recipient name")),
        UNREGISTERED_RECIPIENT(
                new LocalizableKey("Recipient accounts missing from accounts ledger")),
        TRANSFER_MODIFY_AMOUNT(
                new LocalizableKey("It's not possible to modify the amount of this transfer")),
        TRANSFER_MODIFY_DESTINATION(
                new LocalizableKey("It's not possible to modify the destination of this transfer")),
        TRANSFER_MODIFY_DUEDATE(
                new LocalizableKey(
                        "It's not possible to modify the payment date of this transfer")),
        TRANSFER_MODIFY_MESSAGE(
                new LocalizableKey("It's not possible to modify OCR/message of this transfer")),
        TRANSFER_MODIFY_NOT_ALLOWED(
                new LocalizableKey("It's not possible to modify this transfer")),
        TRANSFER_MODIFY_SOURCE_OR_DESTINATION(
                new LocalizableKey(
                        "It's not possible to modify the source or destination account of this transfer")),
        TRANSFER_EXECUTE_FAILED(new LocalizableKey("Could not execute transfer")),
        TRANSFER_DELETE_FAILED(new LocalizableKey("Could not delete transfer")),
        TRANSFER_CONFIRM_FAILED(
                new LocalizableKey("An error occurred when confirming the transfer")),
        TRANSFER_REJECTED(new LocalizableKey("Transfer rejected by the Bank")),
        EINVOICE_MODIFY_AMOUNT(
                new LocalizableKey("It's not possible to modify the amount of this e-invoice")),
        EINVOICE_MODIFY_DUEDATE(
                new LocalizableKey(
                        "It's not possible to modify the payment date of this e-invoice")),
        EINVOICE_MODIFY_DESTINATION(
                new LocalizableKey(
                        "It's not possible to modify the destination account of this e-invoice")),
        EINVOICE_MODIFY_SOURCE_MESSAGE(
                new LocalizableKey(
                        "It's not possible to modify the source message of this e-invoice")),
        EINVOICE_MODIFY_DESTINATION_MESSAGE(
                new LocalizableKey(
                        "It's not possible to modify the destination message of this e-invoice")),
        EINVOICE_MODIFY_SOURCE(
                new LocalizableKey(
                        "It's not possible to modify the source account of this e-invoice")),
        EINVOICE_MODIFY_NOT_ALLOWED(
                new LocalizableKey("It's not possible to modify this e-invoice")),
        PAYMENT_NO_MATCHES(new LocalizableKey("The payment could not be found at your bank")),
        PAYMENT_CREATE_FAILED(new LocalizableKey("Could not create payment")),
        PAYMENT_UPDATE_FAILED(new LocalizableKey("Could not update payment")),
        PAYMENT_UPDATE_NOT_ALLOWED(new LocalizableKey("It's not possible to modify this payment")),
        PAYMENT_UPDATE_AMOUNT(
                new LocalizableKey("It's not possible to modify the amount of this payment")),
        PAYMENT_UPDATE_DESTINATION(
                new LocalizableKey("It's not possible to modify the destination of this payment")),
        PAYMENT_UPDATE_SOURCE(
                new LocalizableKey(
                        "It's not possible to modify the source account of this payment")),
        PAYMENT_UPDATE_DUEDATE(
                new LocalizableKey("It's not possible to modify the payment date of this payment")),
        PAYMENT_UPDATE_DESTINATION_MESSAGE(
                new LocalizableKey("It's not possible to modify OCR/message of this payment")),
        PAYMENT_UPDATE_SOURCE_MESSAGE(
                new LocalizableKey(
                        "It's not possible to modify the source message of this payment")),
        PAYMENT_AUTHORIZATION_FAILED(new LocalizableKey("Payment authorization failed.")),
        PAYMENT_AUTHENTICATION_FAILED(new LocalizableKey("Payment authentication failed.")),
        PAYMENT_CONFIRMATION_FAILED(
                new LocalizableKey("An error occurred while confirming the payment.")),
        PAYMENT_REJECTED(new LocalizableKey("The payment was rejected by the bank.")),
        PAYMENT_CANCELLED(new LocalizableKey("The payment was cancelled by the user.")),
        MISSING_MESSAGE_TYPE(new LocalizableKey("Missing message type")),
        INVALID_STRUCTURED_MESSAGE(new LocalizableKey("The entered structured message is invalid")),
        END_USER_WRONG_PAYMENT_TYPE(
                new LocalizableKey("This type of payment is unavailable for you")),
        COULD_NOT_SAVE_NEW_RECIPIENT_MESSAGE(
                new LocalizableKey(
                        "Could not save new recipient, check that the information is correct")),
        USER_UNAUTHORIZED(new LocalizableKey("User is not authorized to create the payment")),
        GENERIC_PAYMENT_ERROR_MESSAGE(
                new LocalizableKey("There was a problem connecting to the bank."));

        private final LocalizableKey key;

        EndUserMessage(LocalizableKey key) {
            Preconditions.checkNotNull(key);
            this.key = key;
        }

        @Override
        public LocalizableKey getKey() {
            return key;
        }
    }

    public enum EndUserMessageParametrized implements LocalizableParametrizedEnum {
        INVALID_MESSAGE_WHEN_MAX_LENGTH(
                new LocalizableParametrizedKey(
                        "The message length exceeds maximum of {0} letters."));

        private final LocalizableParametrizedKey key;

        EndUserMessageParametrized(LocalizableParametrizedKey key) {
            Preconditions.checkNotNull(key);
            this.key = key;
        }

        @Override
        public LocalizableParametrizedKey getKey() {
            return key;
        }

        @Override
        public LocalizableParametrizedKey cloneWith(Object... parameters) {
            return key.cloneWith(parameters);
        }
    }

    public static void throwIf(boolean condition) {
        if (condition) {
            throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                    .setMessage("Not implemented")
                    .setEndUserMessage("Not implemented")
                    .build();
        }
    }
}
