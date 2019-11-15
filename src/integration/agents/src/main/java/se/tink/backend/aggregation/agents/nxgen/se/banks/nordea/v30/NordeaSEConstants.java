package se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30;

import com.google.common.collect.ImmutableMap;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.models.Instrument;
import se.tink.backend.aggregation.agents.models.Portfolio;
import se.tink.backend.aggregation.agents.utils.log.LogTag;
import se.tink.backend.aggregation.nxgen.core.account.TypeMapper;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.utils.transfer.TransferMessageLengthConfig;

public class NordeaSEConstants {
    public static final int NUM_TRANSACTIONS_PER_PAGE = 200;
    public static final int NUM_CREDIT_CARD_TRANSACTIONS_PER_PAGE = 30;
    public static final String CURRENCY = "SEK";

    public static final ImmutableMap<String, String> DEFAULT_FORM_PARAMS =
            ImmutableMap.<String, String>builder()
                    .put(FormParams.AUTH_METHOD, AuthMethod.BANKID_SE)
                    .put(FormParams.CLIENT_ID, "NDHMSE")
                    .put(FormParams.COUNTRY, "SE")
                    .put(FormParams.GRANT_TYPE, "password")
                    .put(FormParams.SCOPE, "ndf")
                    .build();

    public static final TypeMapper<Instrument.Type> INSTRUMENT_TYPE_MAP =
            TypeMapper.<Instrument.Type>builder()
                    .put(Instrument.Type.FUND, "FUND")
                    .put(Instrument.Type.STOCK, "EQUITY")
                    .build();

    public static final TypeMapper<Portfolio.Type> PORTFOLIO_TYPE_MAP =
            TypeMapper.<Portfolio.Type>builder()
                    .put(Portfolio.Type.DEPOT, "FONDA", "ASBS")
                    .put(Portfolio.Type.ISK, "ISK")
                    .put(Portfolio.Type.PENSION, "ISP", "NLPV2")
                    .build();

    public static TypeMapper<AccountTypes> ACCOUNT_TYPE_MAPPER =
            TypeMapper.<AccountTypes>builder()
                    .put(AccountTypes.CHECKING, "transaction")
                    .put(AccountTypes.SAVINGS, "savings")
                    .put(AccountTypes.CREDIT_CARD, "credit", "combined")
                    .put(AccountTypes.LOAN, "mortgage")
                    .build();

    public static class Urls {
        public static final String BASE = "https://private.nordea.se/api/dbf/";

        public static final URL LOGIN_BANKID = new URL(BASE + ApiService.LOGIN_BANKID);
        public static final URL PASSWORD_TOKEN = new URL(BASE + ApiService.PASSWORD_TOKEN);
        public static final URL FETCH_ACCOUNTS = new URL(BASE + ApiService.FETCH_ACCOUNTS);
        public static final URL FETCH_ACCOUNT_TRANSACTIONS =
                new URL(BASE + ApiService.FETCH_TRANSACTIONS);
        public static final URL FETCH_CARDS = new URL(BASE + ApiService.FETCH_CARDS);
        public static final URL FETCH_CARD_TRANSACTIONS =
                new URL(BASE + ApiService.FETCH_CARD_TRANSACTIONS);
        public static final URL FETCH_INVESTMENTS = new URL(BASE + ApiService.FETCH_INVESTMENTS);
        public static final URL FETCH_LOANS = new URL(BASE + ApiService.FETCH_LOANS);
        public static final URL FETCH_LOAN_DETAILS = new URL(BASE + ApiService.FETCH_LOAN_DETAILS);
        public static final URL FETCH_PAYMENTS = new URL(BASE + ApiService.FETCH_PAYMENTS);
        public static final URL FETCH_PAYMENT_DETAILS =
                new URL(BASE + ApiService.FETCH_PAYMENTS_DETAILS);
        public static final URL FETCH_BENEFICIARIES =
                new URL(BASE + ApiService.FETCH_BENEFICIARIES);
        public static final URL EXECUTE_TRANSFER = new URL(BASE + ApiService.FETCH_PAYMENTS);
        public static final URL CONFIRM_TRANSFER = new URL(BASE + ApiService.CONFIRM_TRANSFER);
        public static final URL SIGN_TRANSFER = new URL(BASE + ApiService.SIGN_TRANSFER);
        public static final URL POLL_SIGN = new URL(BASE + ApiService.POLL_SIGN);
        public static final URL COMPLETE_TRANSFER = new URL(BASE + ApiService.COMPLETE_TRANSFER);
        public static final URL LOGOUT_BANKID = new URL(BASE + ApiService.LOGOUT_BANKID);
        public static final URL UPDATE_PAYMENT = new URL(BASE + ApiService.FETCH_PAYMENTS_DETAILS);
        public static final URL ENROLL = new URL(BASE + ApiService.ENROLL);
        public static final URL CONFIRM_ENROLLMENT = new URL(BASE + ApiService.CONFIRM_ENROLLMENT);
        public static final URL INIT_DEVICE_AUTH = new URL(BASE + ApiService.INIT_DEVICE_AUTH);
        public static final URL COMPLETE_DEVICE_AUTH =
                new URL(BASE + ApiService.COMPLETE_DEVICE_AUTH);
        public static final URL VERIFY_PERSONAL_CODE =
                new URL(BASE + ApiService.VERIFY_PERSONAL_CODE);
        public static final URL LOGOUT_PASSWORD = new URL(BASE + ApiService.LOGIN_PASSWORD);
    }

    public static class ApiService {
        public static final String LOGIN_BANKID =
                "se/authentication-bankid-v1/security/oauth/token";
        public static final String PASSWORD_TOKEN = "ca/token-service-v3/oauth/token";
        public static final String FETCH_ACCOUNTS = "ca/accounts-v1/accounts/";
        public static final String FETCH_TRANSACTIONS =
                "ca/accounts-v1/accounts/{accountNumber}/transactions";
        public static final String FETCH_CARDS = "ca/cards-v2/cards/";
        public static final String FETCH_CARD_TRANSACTIONS =
                "ca/cards-v2/cards/{cardId}/transactions";
        public static final String FETCH_INVESTMENTS = "ca/savings-v1/savings/custodies";
        public static final String FETCH_LOANS = "ca/loans-v1/loans/";
        public static final String FETCH_LOAN_DETAILS = "ca/loans-v1/loans/{loanId}";
        public static final String FETCH_PAYMENTS = "se/payments-v2/payments/";
        public static final String FETCH_PAYMENTS_DETAILS = "se/payments-v2/payments/{paymentId}";
        public static final String FETCH_BENEFICIARIES = "ca/beneficiary-v1/beneficiaries";
        public static final String CONFIRM_TRANSFER = "se/payments-v2/payments/confirm";
        public static final String SIGN_TRANSFER = "ca/signing-v1/signing/bankid_se/signature/";
        public static final String POLL_SIGN =
                "ca/signing-v1/signing/bankid_se/signature/{orderRef}";
        public static final String COMPLETE_TRANSFER =
                "se/payments-v2/payments/complete/{orderRef}";
        public static final String LOGOUT_BANKID = "ca/token-revocation-v1/token/revoke";
        public static final String ENROLL =
                "ca/trusted-applications-v2/applications/{applicationId}/enrolments";
        public static final String CONFIRM_ENROLLMENT =
                "ca/trusted-applications-v2/applications/{applicationId}/enrolments/{enrollmentId}";
        public static final String INIT_DEVICE_AUTH =
                "ca/trusted-applications-v2/applications/{applicationId}/enrolments/{enrollmentId}/nonce";
        public static final String COMPLETE_DEVICE_AUTH =
                "ca/trusted-applications-v2/applications/{applicationId}/enrolments/{enrollmentId}/token";
        public static final String VERIFY_PERSONAL_CODE =
                "ca/personal-code-v1/personal-codes/verify";
        public static final String LOGIN_PASSWORD = "ca/token-service-v3/oauth/token/revoke";
    }

    public static class QueryParams {
        public static final String OFFSET = "offset";
        public static final String LIMIT = "limit";
        public static final String PAGE = "page";
        public static final String PAGE_SIZE = "page_size";
        public static final String PAGE_SIZE_LIMIT =
                String.valueOf(NUM_CREDIT_CARD_TRANSACTIONS_PER_PAGE);
        public static final String POLLING_SEQUENCE = "polling_sequence";
        public static final String STATUS = "status";
        public static final String STATUS_VALUES = "unconfirmed,confirmed,rejected,inprogress";
    }

    public static class FormParams {
        public static final String AUTH_METHOD = "auth_method";
        public static final String CLIENT_ID = "client_id";
        public static final String CODE = "code";
        public static final String COUNTRY = "country";
        public static final String GRANT_TYPE = "grant_type";
        public static final String SCOPE = "scope";
        public static final String USERNAME = "username";
        public static final String TOKEN = "token";
        public static final String TOKEN_TYPE_HINT = "token_type_hint";
        public static final String TOKEN_TYPE = "access_token";
        public static final String REDIRECT_URI = "redirect_uri";
        public static final String CODE_VERIFIER = "code_verifier";
    }

    public static class IdTags {
        public static final String ACCOUNT_NUMBER = "accountNumber";
        public static final String CARD_ID = "cardId";
        public static final String LOAN_ID = "loanId";
        public static final String PAYMENT_ID = "paymentId";
        public static final String ORDER_REF = "orderRef";
        public static final String APPLICATION_ID = "applicationId";
        public static final String ENROLLMENT_ID = "enrollmentId";
    }

    public static class TagValues {
        public static final String APPLICATION_ID = "zrIIeWA0LqJHJJ0ZSZr1";
    }

    public static class StorageKeys {
        public static final String ACCESS_TOKEN = "auth_token";
        public static final String TOKEN_TYPE = "token_type";
        public static final String REFRESH_TOKEN = "refresh_token";
        public static final String SSN = "ssn";
        public static final String TOKEN_AUTH_METHOD = "auth_type";
        public static final String SENSITIVE_PAYLOAD_PASSWORD = "password";
        public static final String DEVICE_TOKEN = "DeviceToken";
        public static final String PERSONAL_CODE_ENROLLMENT_ID = "personalCodeEnrollmentId";
        public static final String DEVICE_PRIVATE_KEY = "devicePrivateKey";
        public static final String DEVICE_PUBLIC_KEY = "devicePublicKey";
        public static final String PERSONAL_CODE_VALID_UNTIL = "personalCodeValidUntil";
        public static final String DEVICE_AUTH_TOKEN = "deviceAuthToken";
    }

    public static final class NordeaBankIdStatus {
        public static final String AGREEMENTS_UNAVAILABLE = "agreements_unavailable";
        public static final String EXTERNAL_AUTHENTICATION_REQUIRED =
                "external_authentication_required";
        public static final String AUTHENTICATION_CANCELLED = "authentication_cancelled";
        public static final String EXTERNAL_AUTHENTICATION_PENDING =
                "external_authentication_pending";
        public static final String SIGN_PENDING = "SIGN_PENDING";
        public static final String PENDING = "PENDING";
        public static final String CANCELLED = "CANCELLED";
        public static final String OK = "OK";
    }

    public static final class HeaderParams {
        public static final String LANGUAGE = "en-SE";
    }

    public static final class Fetcher {
        public static final int START_PAGE = 1;
        public static final int CAN_FETCH_MORE = NUM_CREDIT_CARD_TRANSACTIONS_PER_PAGE;
    }

    public static final class PaymentStatus {
        public static final String CONFIRMED = "confirmed";
        public static final String REJECTED = "rejected";
        public static final String UNCONFIRMED = "unconfirmed";
        public static final String INPROGRESS = "inprogress";
        public static final String PAID = "paid";
    }

    public static final class PaymentTypes {
        public static final String BANKGIRO = "bankgiro";
        public static final String PLUSGIRO = "plusgiro";
        public static final String EINVOICE = "einvoice";
        public static final String IBAN = "iban";
        public static final String LBAN = "lban";
        public static final String NORMAL = "normal";
        public static final String UNKNOWN_TYPE = "UNKNOWN TYPE";
    }

    public static final class PaymentAccountTypes {
        public static final String BANKGIRO = "BG";
        public static final String PLUSGIRO = "PG";
        public static final String LBAN = "LBAN-SE";
        public static final String NDASE = "NDA-SE";
        public static final String UNKNOWN_ACCOUNT_TYPE = "UNKNOWN ACCOUNT TYPE";
    }

    public static final class Transfer {
        public static final TransferMessageLengthConfig TRANSFER_MESSAGE_LENGTH_CONFIG =
                TransferMessageLengthConfig.createWithMaxLength(50, 12, 50);
        public static final String WHITE_LISTED_CHARACTERS = ".,?'-/:()+";
        public static final String SPEED = "normal";
        public static final String OWN_TRANSFER = "owntransfer";
        public static final String TO_ACCOUNT_TYPE = "NDA-SE";
        public static final int MAX_POLL_ATTEMPTS = 90;
        public static final String RECIPIENT_NAME_FIELD_NAME = "name";
        public static final String RECIPIENT_NAME_FIELD_DESCRIPTION = "Mottagarnamn";
    }

    public static class LogMessages {
        public static final String EINVOICE_NOT_FOUND =
                "Could not find the selected invoice in user's bank.";
        public static final String NO_RECIPIENT_NAME = "Could not get recipient name from user";
        public static final String NO_INVESTMENTS = "User has no agreement for investments";
        public static final String NO_CONFIRMED_INVESTMENTS =
                "User has not confirmed classification for investments";
        public static final String NO_CUSTODY_ACCOUNT = "No account connected to custody account";
        public static final String EINVOICE_MODIFY_AMOUNT =
                "Not allowed to update e-invoice amount";
        public static final String EINVOICE_MODIFY_DESTINATION_MESSAGE =
                "Not allowed to update e-invoice message";
        public static final String EINVOICE_MODIFY_DUEDATE =
                "Not allowed to update e-invoice due date";
        public static final String EINVOICE_MODIFY_SOURCE =
                "Not allowed to update e-invoice source";
        public static final String EINVOICE_MODIFY_DESTINATION =
                "Not allowed to update e-invoice destination";
        public static final String DUPLICATE_PAYMENT =
                "This payment seems to be duplicate. Please check your outgoing payments in the banks app.";
        public static final String BANKSIDE_ERROR_WHEN_SEARCHING_OUTBOX =
                "Error from bank when trying to fetch details about payment outbox";
    }

    public static class TransactionalAccounts {
        public static final String PERSONAL_ACCOUNT = "PERSONKONTO";
        public static final String NORDEA_CLEARING_NUMBER = "3300";
    }

    public static class InvestmentAccounts {
        public static final String PRODUCT_NAME = "ISK";
    }

    public static class ErrorCodes {
        // token required
        public static final String TOKEN_REQUIRED = "token_required";

        // user has no agreement (for investments)
        public static final String AGREEMENT_NOT_CONFIRMED =
                "RBO_ACCESS_DENIED_AGREEMENT_NOT_CONFIRMED";
        public static final String CLASSIFICATION_NOT_CONFIRMED =
                "RBO_ACCESS_DENIED_CLASSIFICATION_NOT_CONFIRMED";

        // user has no account connected to depot, cannot fetch investments
        public static final String UNABLE_TO_LOAD_CUSTOMER = "ERROR_OSIA_UNABLE_TO_LOAD_CUSTOMER";
        public static final String OWNER_NOT_FOUND = "No owner account name found";
        // access token has expired
        public static final String INVALID_TOKEN = "invalid_token";
        // refresh token has expired
        public static final String INVALID_GRANT = "invalid_grant";
        public static final String AUTHENTICATION_COLLISION = "authentication_collision";
        public static final String AUTHENTICATION_FAILED = "authentication_failed";
        public static final String UNABLE_TO_FETCH_ACCOUNTS = "Could not retrieve accounts.";
        public static final String DUPLICATE_PAYMENT =
                "Duplicate payment. Technical code. Please try again.";
        public static final String TRANSFER_REJECTED = "Transfer rejected by Nordea";
        public static final String TRANSFER_ERROR = "Something went wrong with the transfer.";
        public static final String PAYMENT_ERROR = "Something went wrong with the payment.";
        public static final String UNREGISTERED_RECIPIENT =
                "Recipient accounts missing from accounts ledger";
        public static final String NOT_ENOUGH_FUNDS = "Not enough funds";
        public static final String EXTERNAL_SERVICE_CALL_FAILED = "External service call failed";
    }

    public static class LogTags {
        public static final LogTag CREDIT_TRANSACTIONS_ERROR =
                LogTag.from("NORDEA_SE_TRANSACTIONS_ERROR");
        public static final LogTag LOAN_ACCOUNT = LogTag.from("NORDEA_SE_LOAN_ACCOUNT");
    }

    public class AuthMethod {
        public static final String NASA = "nasa";
        public static final String BANKID_SE = "bankid_se";
    }

    public class EnrollmentState {
        public static final String ACTIVE = "active";
    }

    public class EnrollmentType {
        public static final String PERSONAL_CODE = "personal_code";
    }
}
