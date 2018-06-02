package se.tink.backend.grpc.v1.errors;

import io.grpc.Status;
import org.slf4j.event.Level;
import se.tink.backend.main.validators.exception.AbstractTransferException;
import se.tink.libraries.i18n.LocalizableKey;

public class ApiError {
    private final LocalizableKey userMessage;
    private final Status.Code status;
    private final String tag;
    private final Throwable cause;
    private final Level severity;

    ApiError(Status.Code status, String tag, LocalizableKey userMessage) {
        this(status, tag, userMessage, Level.ERROR, null);
    }

    ApiError(Status.Code status, String tag, LocalizableKey userMessage, Level severity, Throwable cause) {
        this.status = status;
        this.userMessage = userMessage;
        this.tag = tag;
        this.cause = cause;
        this.severity = severity;
    }

    public ApiError withCause(Throwable cause) {
        return new ApiError(status, tag, userMessage, severity, cause);
    }

    public ApiError withWarnSeverity() {
        return new ApiError(status, tag, userMessage, Level.WARN, cause);
    }

    public ApiError withInfoSeverity() {
        return new ApiError(status, tag, userMessage, Level.INFO, cause);
    }

    public ApiException exception() {
        return new ApiException(status, tag, userMessage, severity, cause);
    }

    public static ApiError NOT_IMPLEMENTED = new ApiError(Status.Code.UNIMPLEMENTED,
            "unimplemented", new LocalizableKey("Feature is not implemented."));

    /**
     * Errors related to authentication.
     */
    public static class Authentication {
        public static ApiError DEPRECATED_CLIENT = new ApiError(Status.Code.UNAUTHENTICATED,
                "authentication.deprecated_client", new LocalizableKey("Client is deprecated."));

        public static ApiError UNAUTHORIZED_DEVICE = new ApiError(Status.Code.UNAUTHENTICATED,
                "authentication.unauthorized_device", new LocalizableKey("Device is not authorized."));

        public static ApiError USER_ALREADY_REGISTERED = new ApiError(Status.Code.ALREADY_EXISTS,
                "authentication.user_already_registered",
                new LocalizableKey("A user with this username is already registered."));

        public static ApiError UNAUTHENTICATED = new ApiError(Status.Code.UNAUTHENTICATED,
                "authentication.unauthenticated",
                new LocalizableKey("User is not authenticated."));

        public static ApiError INTERNAL_ERROR = new ApiError(Status.Code.UNAUTHENTICATED,
                "authentication.internal_error",
                new LocalizableKey("Internal error."));

        /**
         * Generic errors for authentication tokens.
         */
        public static class AuthenticationToken {
            public static ApiError NOT_FOUND = new ApiError(Status.Code.UNAUTHENTICATED,
                    "authentication.authentication_token.not_found",
                    new LocalizableKey("Authentication token not found."));

            public static ApiError EXPIRED = new ApiError(Status.Code.UNAUTHENTICATED,
                    "authentication.authentication_token.expired",
                    new LocalizableKey("Authentication token has expired."));

            public static ApiError INVALID = new ApiError(Status.Code.UNAUTHENTICATED,
                    "authentication.authentication_token.invalid_status",
                    new LocalizableKey("Authentication token has an invalid status."));

            public static ApiError GROWTH_USER_NOT_ALLOWED = new ApiError(Status.Code.UNAUTHENTICATED,
                    "authentication.authentication_token.api_customers_not_allowed",
                    new LocalizableKey(
                            "API customers are not allowed to login. Please sign-up with new credentials to be able to use the app."));
        }

        /**
         * Errors related to mobile bankid authentication.
         */
        public static class MobileBankId {
            public static ApiError NOT_FOUND = new ApiError(Status.Code.UNAUTHENTICATED,
                    "authentication.mobile_bank_id.not_found", new LocalizableKey("Mobile BankId token not found."));

            public static ApiError EXPIRED = new ApiError(Status.Code.UNAUTHENTICATED,
                    "authentication.mobile_bank_id.expired", new LocalizableKey("Mobile BankId token expired."));

            public static ApiError TIMED_OUT = new ApiError(Status.Code.DEADLINE_EXCEEDED,
                    "authentication.mobile_bank_id.timed_out", new LocalizableKey("Mobile BankId timed out."));

            public static ApiError ABORTED = new ApiError(Status.Code.ABORTED,
                    "authentication.mobile_bank_id.aborted", new LocalizableKey("Mobile BankId aborted."));
        }

        /**
         * Generic errors for SMS OTPs.
         */
        public static class SmsOtp {
            public static ApiError NOT_FOUND = new ApiError(Status.Code.NOT_FOUND,
                    "sms_otp.not_found", new LocalizableKey("SMS OTP was not found."));
            public static ApiError INVALID_STATUS = new ApiError(Status.Code.INVALID_ARGUMENT,
                    "sms_otp.invalid_status", new LocalizableKey("SMS OTP has an invalid status."));
            public static ApiError GATEWAY_UNAVAILABLE = new ApiError(Status.Code.UNAVAILABLE,
                    "sms_otp.gateway_unavailable", new LocalizableKey("SMS OTP gateway is currently unavailable."));
            public static ApiError PHONE_NUMBER_BLOCKED = new ApiError(Status.Code.RESOURCE_EXHAUSTED,
                    "sms_otp.phone_number_blocked", new LocalizableKey("Phone number is currently blocked."));
        }

        /**
         * Generic errors for challenges.
         */
        public static class Challenge {
            public static ApiError NOT_FOUND = new ApiError(Status.Code.NOT_FOUND,
                    "authentication.challenge.not_found", new LocalizableKey("Challenge was not found."));
            public static ApiError EXPIRED = new ApiError(Status.Code.DEADLINE_EXCEEDED,
                    "authentication.challenge.expired", new LocalizableKey("Challenge expired."));
        }
    }

    /**
     * Generic errors for input validation.
     */
    public static class Validation {
        public static ApiError INVALID_LOCALE = new ApiError(Status.Code.INVALID_ARGUMENT,
                "validation.invalid_locale", new LocalizableKey("Locale is not valid."));

        public static ApiError INVALID_EMAIL = new ApiError(Status.Code.INVALID_ARGUMENT,
                "validation.invalid_email", new LocalizableKey("Email is not valid."));

        public static ApiError INVALID_PHONE_NUMBER = new ApiError(Status.Code.INVALID_ARGUMENT,
                "validation.invalid_phone_number", new LocalizableKey("Phone number is not valid."));

        public static ApiError INVALID_PIN_6 = new ApiError(Status.Code.INVALID_ARGUMENT,
                "validation.invalid_pin_6", new LocalizableKey("PIN6 is not valid."));
    }

    /**
     * Generic errors for transfers.
     */
    public static class Transfers {
        public static ApiError PERMISSION_DENIED = new ApiError(Status.Code.PERMISSION_DENIED,
                "transfer.permission_denied", new LocalizableKey("Access denied for requested transfer."));

        public static ApiError SERVICE_TEMPORARY_DISABLED = new ApiError(Status.Code.UNAVAILABLE,
                "transfers.service_temporary_unavailable",
                AbstractTransferException.EndUserMessage.TEMPORARY_DISABLED.getKey());

        public static ApiError NOT_FOUND = new ApiError(Status.Code.UNAVAILABLE,
                "transfers.not_found", new LocalizableKey("Transfer doesn't exist."));

        public static class Giro {
            public static ApiError NOT_FOUND = new ApiError(Status.Code.NOT_FOUND,
                    "transfers.giro.not_found", new LocalizableKey("Couldn't find giro account."));

            public static ApiError INVALID_GIRO = new ApiError(Status.Code.INVALID_ARGUMENT,
                    "transfers.giro.invalid_giro", new LocalizableKey("Not a valid giro number."));
        }

        public static class Destinations {
            public static ApiError ALREADY_EXISTS = new ApiError(Status.Code.ALREADY_EXISTS,
                    "transfers.transfer_destinations.already_exists",
                    new LocalizableKey("Transfer destination already exists."));
        }
    }

    /**
     * Generic errors for accounts.
     */
    public static class Accounts {
        public static ApiError NOT_FOUND = new ApiError(Status.Code.NOT_FOUND, "account.not_found",
                new LocalizableKey("Account was not found."));
    }

    /**
     * Generic errors for users.
     */
    public static class Users {
        public static ApiError NOT_FOUND = new ApiError(Status.Code.NOT_FOUND, "user.not_found",
                new LocalizableKey("User was not found."));
    }

    /**
     * Errors related to statistics.
     */
    public static class Statistics {
        public static ApiError UNAVAILABLE = new ApiError(Status.Code.UNAVAILABLE, "statistic.unavailable",
                new LocalizableKey("Statistics are currently unavailable."));
    }

    /**
     * Errors related to activities.
     */
    public static class Activities {
        public static ApiError UNAVAILABLE = new ApiError(Status.Code.UNAVAILABLE, "activities.unavailable",
                new LocalizableKey("Activities are currently unavailable."));
    }

    /**
     * Errors related to credentials.
     */
    public static class Credentials {
        public static ApiError ALREADY_EXIST = new ApiError(Status.Code.ALREADY_EXISTS,
                "credentials.already_exist",
                new LocalizableKey("A credential with the same fields is already created."));

        public static ApiError PERMISSION_DENIED = new ApiError(Status.Code.PERMISSION_DENIED,
                "credentials.permission_denied",
                new LocalizableKey("The credential is not allowed to be created."));

        public static ApiError UNKNOWN_ERROR = new ApiError(Status.Code.INTERNAL,
                "credentials.unknown_error",
                new LocalizableKey("An unknown error occurred when the credential was created."));
    }

    /**
     * Errors related to consents.
     */
    public static class Consents {
        public static ApiError NOT_FOUND = new ApiError(Status.Code.NOT_FOUND,
                "consents.not_found", new LocalizableKey("The consent was not found."));

        public static ApiError INVALID_CHECKSUM = new ApiError(Status.Code.FAILED_PRECONDITION,
                "consents.invalid_checksum", new LocalizableKey("The checksum doesn't match."));

        public static ApiError INVALID_REQUEST = new ApiError(Status.Code.INTERNAL,
                "credentials.invalid_request", new LocalizableKey("The request is not valid."));
    }

    /**
     * Errors related to transactions.
     */
    public static class Transactions {
        public static ApiError UNKNOWN_CATEGORY = new ApiError(Status.Code.INVALID_ARGUMENT,
                "transactions.unknown_category", new LocalizableKey("The category is not found."));

        public static ApiError UNAVAILABLE = new ApiError(Status.Code.UNAVAILABLE,
                "transactions.unavailable", new LocalizableKey("The service is currently unavailable."));

        public static ApiError NOT_FOUND = new ApiError(Status.Code.NOT_FOUND,
                "transaction.not_found", new LocalizableKey("The transaction could not be found."));
    }

    /**
     * Errors related to user identity.
     */
    public static class Identity {
        public static ApiError NOT_FOUND = new ApiError(Status.Code.NOT_FOUND,
                "identity.not_found", new LocalizableKey("Couldn't find any identity events."));

        public static ApiError INTERNAL_ERROR = new ApiError(Status.Code.INTERNAL,
                "identity.internal_error", new LocalizableKey("The service is currently unavailable."));
    }

    /**
     * Errors related to streaming of data between server and clients.
     */
    public static class Streaming {
        public static ApiError ABORTED = new ApiError(Status.Code.ABORTED,
                "streaming.aborted", new LocalizableKey("The stream is aborted by the client."));

        public static ApiError INTERNAL_ERROR = new ApiError(Status.Code.INTERNAL,
                "streaming.internal_error", new LocalizableKey("The service is currently unavailable."));
    }

    /**
     * Errors related to the ABN AMRO migration
     */
    public static class AbnAmroMigration {
        public static ApiError ALREADY_MIGRATED = new ApiError(Status.Code.INVALID_ARGUMENT,
                "abn_amro_migration.user_already_migrated", new LocalizableKey("User is already migrated."));
    }

    /**
     * Errors related to the Applications/Forms flow.
     */
    public static class Applications {
        public static ApiError NOT_FOUND = new ApiError(Status.Code.NOT_FOUND,
                "applications.application_not_found", new LocalizableKey("Application was not found."));

        public static ApiError APPLICATION_NOT_VALID = new ApiError(Status.Code.INTERNAL,
                "applications.application_not_valid", new LocalizableKey("Failed to get application."));

        public static ApiError FAILED_TO_SUBMIT_FORM = new ApiError(Status.Code.INTERNAL,
                "applications.form_submit_failed", new LocalizableKey("Failed to submit form."));

        public static ApiError FORBIDDEN = new ApiError(Status.Code.RESOURCE_EXHAUSTED,
                "applications.application_already_signed",
                new LocalizableKey("The application is already signed and cannot be modified"));

        public static ApiError INTERNAL_ERROR = new ApiError(Status.Code.INTERNAL,
                "applications.internal_error", new LocalizableKey("Something went wrong."));

        public static ApiError SIGNING_NOT_INVOKABLE = new ApiError(Status.Code.INTERNAL,
                "applications.application_signing_not_invokable",
                new LocalizableKey("Unable to invoke application signing."));

        public static ApiError COULD_NOT_PROCESS = new ApiError(Status.Code.INTERNAL,
                "applications.application_not_valid", new LocalizableKey("Could not process application"));

        public static ApiError APPLICATION_NOT_COMPLETE = new ApiError(Status.Code.FAILED_PRECONDITION,
                "applications.application_not_complete", new LocalizableKey("Application hasn't been completed yet."));

        public static ApiError PERMISSION_DENIED = new ApiError(Status.Code.PERMISSION_DENIED,
                "applications.permission_denied", new LocalizableKey("Permission denied."));
    }

    /**
     * Errors related to properties
     */
    public static class Properties {
        public static ApiError NOT_FOUND = new ApiError(Status.Code.NOT_FOUND,
                "properties.not_found", new LocalizableKey("Property not found."));
    }

    /**
     * Errors related to Follow service
     */
    public static class FollowItems {
        public static ApiError BAD_REQUEST = new ApiError(Status.Code.INVALID_ARGUMENT, "follow_items.illegal_argument",
                new LocalizableKey("Illegal argument"));

        public static ApiError CONFLICT = new ApiError(Status.Code.ALREADY_EXISTS, "follow_items.already_exist",
                new LocalizableKey("Follow item already exist"));

        public static ApiError NOT_FOUND = new ApiError(Status.Code.NOT_FOUND, "follow_items.not_found",
                new LocalizableKey("Follow item was not found"));
    }

    public static class PublicKeys {
        public static ApiError NOT_FOUND = new ApiError(Status.Code.NOT_FOUND, "public_keys.not_found",
                new LocalizableKey("Public key was not found."));
    }
}
