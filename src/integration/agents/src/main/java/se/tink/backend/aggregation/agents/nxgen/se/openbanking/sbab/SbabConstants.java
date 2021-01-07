package se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab;

import java.util.regex.Pattern;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public final class SbabConstants {

    private SbabConstants() {
        throw new AssertionError();
    }

    public static class ErrorMessages {
        public static final String INVALID_CONFIGURATION =
                "Invalid Configuration: %s cannot be empty or null";
    }

    public static class Urls {
        public static final String BASE_URL = "https://psd.sbab.se/psd2";

        public static final URL AUTHORIZATION = new URL(BASE_URL + "/auth/1.0/authorize");
        public static final URL AUTHENTICATION = new URL(BASE_URL + "/auth/1.0/authenticate");
        public static final URL TOKEN = new URL(BASE_URL + "/auth/1.0/token");

        public static final URL CUSTOMERS = new URL(BASE_URL + "/customer/1.0/customers");
        public static final URL ACCOUNTS = new URL(BASE_URL + "/savings/2.0/accounts");
        public static final URL TRANSACTIONS =
                new URL(BASE_URL + "/savings/2.0/accounts/{accountNumber}/transfers");

        public static final URL INITIATE_PAYMENT =
                new URL(BASE_URL + "/savings/2.0/accounts/{accountNumber}/transfers");
        public static final URL GET_PAYMENT =
                new URL(
                        BASE_URL
                                + "/savings/2.0/accounts/{accountNumber}/transfers/status/{referenceId}");
    }

    public static class StorageKeys {
        public static final String ACCOUNT_NUMBER = "accountNumber";
        public static final String OAUTH_TOKEN = "OAUTH_TOKEN";
    }

    public static class QueryKeys {
        public static final String START_DATE = "startDate";
        public static final String END_DATE = "endDate";
        public static final String USER_ID = "user_id";
    }

    public static class QueryValues {
        public static final String PENDING_AUTHORIZATION_CODE = "pending_authorization_code";
        public static final String REFRESH_TOKEN = "refresh_token";
    }

    public static class HeaderKeys {
        public static final String AUTHORIZATION = "Authorization";
        public static final String PSU_IP_ADDRESS = "PSU-IP-Address";
    }

    public static class Format {
        public static final String TIMEZONE = "UTC";
        public static final String TIMESTAMP = "yyyy-MM-dd";
    }

    public static class IdTags {
        public static final String ACCOUNT_NUMBER = "accountNumber";
        public static final String PAYMENT_ID = "referenceId";
    }

    public static class BankIdStatusCodes {
        public static final String AUTHORIZATION_NOT_COMPLETED = "authorization_not_completed";
        public static final String USER_NOT_FOUND = "user_not_found";
        public static final String AUTHORIZATION_FAILED = "authorization_failed";
    }

    public class FormKeys {
        public static final String GRANT_TYPE = "grant_type";
        public static final String REDIRECT_URI = "redirect_uri";
        public static final String CODE = "pending_code";
        public static final String REFRESH_TOKEN = "refresh_token";
    }

    public static class Errors {
        public static final String UNAUTHORIZED_CLIENT = "unauthorized_client";
        public static final String KYC_QUESTIONS_NOT_COMPLETED = "kyc_questions_not_completed";
        public static final String SIGNATURE_FAILED = "SIGNATURE_FAILED";
        public static final String AMOUNT_LIMIT_REACHED = "AMOUNT_LIMIT_REACHED";
        public static final String AMOUNT_EXCEEDS_BALANCE_VALIDATION =
                "AMOUNT_EXCEEDS_BALANCE_VALIDATION";
        public static final String FORBIDDEN = "forbidden";
    }

    public static class ErrorMessage {
        public static final String PAYMENT_REF_TOO_LONG =
                "Supplied payment reference is too long, max is %s characters.";
        public static final String PAYMENT_REF_ILLEGAL_CHARS =
                "Supplied destination message contains illegal characters.";
        // Keeping the error message vague (and unfortunately unhelpful) since we're currently
        // waiting for more details from SBAB support.
        public static final String INVALID_DATE =
                "Supplied date is not valid, ensure it's not set to a non business day and try again.";
        public static final String KYC_MESSAGE =
                "To continue using this app you must answer some questions from your bank. Please log in with your bank's app or website.";
        public static final String SIGNATURE_FAILED = "Payment signing process failed.";
        public static final String AMOUNT_LIMIT_REACHED =
                "The entered amount exceeds the daily limit.";
        public static final String AMOUNT_EXCEEDS_BALANCE =
                "This amount would exceed the available balance and cause overdraft.";
        public static final String FORBIDDEN = "You are not authorised to make this payment";
    }

    public static class HttpClient {
        public static final int MAX_RETRIES = 5;
        public static final int RETRY_SLEEP_MILLISECONDS = 2000;
    }

    public static class PaymentValue {
        // From SBAB documentation
        public static final Pattern ALLOWED_CHARS_PATTERN =
                Pattern.compile("^[a-zA-Z0-9 åäöÅÄÖ!\\-+%\"/?,.§]+$");
        public static final int MAX_DEST_MSG_LEN = 12;

        // Amount related propertyPath
        public static final String AMOUNT = "amount";

        // Invalid payment date
        public static final String TRANSACTION_DATE = "transactionDate";
        public static final String UNKNOWN = "UNKNOWN";
    }
}
