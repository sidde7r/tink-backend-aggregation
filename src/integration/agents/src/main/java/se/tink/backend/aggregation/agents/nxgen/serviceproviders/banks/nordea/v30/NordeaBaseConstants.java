package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30;

import com.google.common.collect.ImmutableMap;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.utils.log.LogTag;
import se.tink.backend.aggregation.nxgen.core.account.TypeMapper;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.instrument.InstrumentModule.InstrumentType;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.portfolio.PortfolioModule.PortfolioType;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.utils.transfer.TransferMessageLengthConfig;

public class NordeaBaseConstants {
    public static final String CURRENCY = "SEK";
    public static final String PROUDCT_CODE = "product_code";

    private NordeaBaseConstants() {
        throw new IllegalStateException("Utility class");
    }

    public static final ImmutableMap<String, String> DEFAULT_FORM_PARAMS =
            ImmutableMap.<String, String>builder()
                    .put(FormParams.AUTH_METHOD, AuthMethod.BANKID_SE)
                    .put(FormParams.CLIENT_ID, FormParams.CLIENT_ID_VALUE)
                    .put(FormParams.COUNTRY, FormParams.COUNTRY_VALUE)
                    .put(FormParams.GRANT_TYPE, "password")
                    .put(FormParams.SCOPE, TagValues.SCOPE_VALUE)
                    .build();
    public static final ImmutableMap<String, String> REQUEST_TOKEN_FORM =
            ImmutableMap.<String, String>builder()
                    .put(FormParams.AUTH_METHOD, AuthMethod.BANKID_SE)
                    .put(FormParams.COUNTRY, FormParams.COUNTRY_VALUE)
                    .put(FormParams.GRANT_TYPE, "authorization_code")
                    .put(FormParams.SCOPE, TagValues.SCOPE_VALUE)
                    .build();

    public static final ImmutableMap<String, String> REFRESH_TOKEN_FORM =
            ImmutableMap.<String, String>builder()
                    .put(FormParams.CLIENT_ID, FormParams.CLIENT_ID_VALUE)
                    .build();

    public static final TypeMapper<InstrumentType> INSTRUMENT_TYPE_MAP =
            TypeMapper.<InstrumentType>builder()
                    .put(InstrumentType.FUND, "FUND")
                    .put(InstrumentType.STOCK, "EQUITY")
                    .ignoreKeys("CASH", "OTHER")
                    .build();

    public static TypeMapper<PortfolioType> PORTFOLIO_TYPE_MAPPER =
            TypeMapper.<PortfolioType>builder()
                    .put(PortfolioType.DEPOT, "FONDA", "ASBS", "PBI", "AKTIV")
                    .put(PortfolioType.ISK, "ISK")
                    .put(PortfolioType.PENSION, "ISP", "NLP", "IPS")
                    .build();

    public static TypeMapper<AccountTypes> ACCOUNT_TYPE_MAPPER =
            TypeMapper.<AccountTypes>builder()
                    .put(AccountTypes.CHECKING, "transaction")
                    .put(AccountTypes.SAVINGS, "savings")
                    .put(AccountTypes.CREDIT_CARD, "credit", "combined")
                    .put(AccountTypes.LOAN, "mortgage")
                    .build();

    public static final ImmutableMap<String, Object> NORDEA_BUSINESS_HEADERS =
            ImmutableMap.<String, Object>builder()
                    .put("x-App-Country", "SE")
                    .put("x-App-Language", "en_SE")
                    .put("x-App-Version", "3.14.0.148")
                    .put("x-Device-Make", "Apple")
                    .put("x-Device-Model", "iPhone9,4")
                    .put("x-Platform-Type", "iOS")
                    .put("x-Platform-Version", "13.3.1")
                    .put("x-app-segment", "corporate")
                    .put("x-device-ec", 1)
                    .put("x-Device-Id", "934B5E23-119E-4E8F-BE66-D7D3B285F744")
                    .put(
                            "User-Agent",
                            "com.nordea.SMEMobileBank.se/3.14.0.148 (Apple iPhone9,4; iOS 13.3.1)")
                    .build();

    public static class Urls {
        public static URL getUrl(String baseUrl, String path) {
            return new URL(baseUrl + path);
        }

        public static final String LOGIN_BANKID_AUTOSTART =
                "ca/bankidse-v1/bankidse/authentications/";
        public static final String FETCH_LOGIN_CODE =
                "ca/user-accounts-service-v1/user-accounts/primary/authorization";
        public static final String FETCH_LOGIN_CODE_WITH_AGREEMENT_ID =
                "ca/user-accounts-service-v1/user-accounts/{agreementId}/authorization";
        public static final String FETCH_ACCESS_TOKEN = "ca/token-service-v3/oauth/token";
        public static final String LOGIN_BANKID =
                "se/authentication-bankid-v1/security/oauth/token";
        public static final String PASSWORD_TOKEN = "ca/token-service-v3/oauth/token";
        public static final String FETCH_ACCOUNTS = "ca/accounts-v3/accounts/";
        public static final String FETCH_TRANSACTIONS =
                "ca/accounts-v3/accounts/{accountNumber}/transactions";
        public static final String FETCH_CARDS = "ca/cards-v4/cards/";
        public static final String FETCH_CARD_TRANSACTIONS =
                "ca/cards-v4/cards/{cardId}/transactions";
        public static final String FETCH_INVESTMENTS = "ca/savings-v1/savings/custodies";
        public static final String FETCH_LOANS = "ca/loans-v1/loans/";
        public static final String FETCH_LOAN_DETAILS = "ca/loans-v1/loans/{loanId}";
        public static final String FETCH_IDENTITY_DATA = "se/customerinfo-v3/customers/self/info";
        public static final String FETCH_PAYMENTS = "ca/payments-v2/payments/";
        public static final String FETCH_PAYMENTS_DETAILS = "ca/payments-v2/payments/{paymentId}";
        public static final String FETCH_BENEFICIARIES = "ca/beneficiary-v1/beneficiaries";
        public static final String CONFIRM_TRANSFER = "ca/payments-v2/payments/confirm";
        public static final String SIGN_TRANSFER = "ca/signing-v1/signing/bankid_se/signature/";
        public static final String POLL_SIGN =
                "ca/signing-v1/signing/bankid_se/signature/{orderRef}";
        public static final String COMPLETE_TRANSFER =
                "ca/payments-v2/payments/complete/{orderRef}";
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
        public static final String START_DATE = "start_date";
        public static final String END_DATE = "end_date";
        public static final String CONTINUATION_KEY = "continuation_key";
        public static final String PAGE = "page";
        public static final String PAGE_SIZE = "page_size";
        public static final String PAGE_SIZE_LIMIT =
                String.valueOf(TransactionFetching.NUM_CREDIT_CARD_TRANSACTIONS_PER_PAGE);
        public static final String POLLING_SEQUENCE = "polling_sequence";
        public static final String STATUS = "status";
        public static final String STATUS_VALUES = "unconfirmed,confirmed,rejected,inprogress";
    }

    public static class TransactionFetching {
        public static final int MAX_CONSECUTIVE_EMPTY_PAGES = 3;
        public static final int MONTHS_TO_PAGINATE = 3;
        public static final int NUM_CREDIT_CARD_TRANSACTIONS_PER_PAGE = 30;
    }

    public static class Headers {
        private Headers() {}

        public static final String REQUEST_ID = "x-Request-Id";
        public static final String REFERER = "Referer";
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
        public static final String COUNTRY_VALUE = "SE";
        public static final String CLIENT_ID_VALUE = "NDHMSE";
    }

    public static class IdTags {
        public static final String ACCOUNT_NUMBER = "accountNumber";
        public static final String CARD_ID = "cardId";
        public static final String LOAN_ID = "loanId";
        public static final String PAYMENT_ID = "paymentId";
        public static final String ORDER_REF = "orderRef";
        public static final String APPLICATION_ID = "applicationId";
        public static final String ENROLLMENT_ID = "enrollmentId";
        public static final String AGREEMENT_ID = "agreementId";
    }

    public static class TagValues {
        public static final String SCOPE_VALUE = "ndf";
    }

    public static class StorageKeys {
        public static final String ACCESS_TOKEN = "auth_token";
        public static final String TOKEN_TYPE = "token_type";
        public static final String REFRESH_TOKEN = "refresh_token";
        public static final String ID_TOKEN = "id_token";
        public static final String UH = "uh";
        public static final String SSN = "ssn";
        public static final String TOKEN_AUTH_METHOD = "auth_type";
        public static final String SENSITIVE_PAYLOAD_PASSWORD = "password";
        public static final String PERSONAL_CODE_ENROLLMENT_ID = "personalCodeEnrollmentId";
        public static final String DEVICE_PRIVATE_KEY = "devicePrivateKey";
        public static final String DEVICE_PUBLIC_KEY = "devicePublicKey";
        public static final String PERSONAL_CODE_VALID_UNTIL = "personalCodeValidUntil";
        public static final String DEVICE_AUTH_TOKEN = "deviceAuthToken";
        public static final String HOLDER_NAME = "holder_name";
    }

    public static final class NordeaBankIdStatus {
        public static final String BANKID_AUTOSTART_PENDING = "assignment_pending";
        public static final String BANKID_AUTOSTART_SIGN_PENDING = "confirmation_pending";
        public static final String BANKID_AUTOSTART_COMPLETED = "completed";
        public static final String BANKID_AUTOSTART_CANCELLED = "cancelled";
        public static final String SIGN_PENDING = "SIGN_PENDING";
        public static final String PENDING = "PENDING";
        public static final String CANCELLED = "CANCELLED";
        public static final String OK = "OK";
    }

    public static final class HeaderParams {
        public static final String LANGUAGE = "en-SE";
        public static final String BUSINESS_REFERER_VALUE =
                "https://corporate.nordea.se/inapp?app_channel=NDCM_SE_IOS&consent_insight=true&consent_marketing=true";
    }

    public static final class Fetcher {
        public static final int START_PAGE = 1;
        public static final int CAN_FETCH_MORE =
                TransactionFetching.NUM_CREDIT_CARD_TRANSACTIONS_PER_PAGE;
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
                TransferMessageLengthConfig.createWithMaxLength(20, 12, 20);
        public static final String WHITE_LISTED_CHARACTERS = ".,?'-/:()+";
        public static final String SPEED = "normal";
        public static final String OWN_TRANSFER = "owntransfer";
        public static final String TO_ACCOUNT_TYPE = "NDA-SE";
        public static final int MAX_POLL_ATTEMPTS = 70;
    }

    public static class LogMessages {
        public static final String EINVOICE_NOT_FOUND =
                "Could not find the selected invoice in user's bank.";
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
        public static final String BANKSIDE_ERROR_WHEN_SEARCHING_OUTBOX =
                "Error from bank when trying to fetch details about payment outbox";
        public static final String WRONG_TO_ACCOUNT_LENGTH =
                "Invalid destination account number, it is too long.";
        public static final String WRONG_OCR_MESSAGE = "Error in reference number (OCR)";
        public static final String USER_UNAUTHORIZED_MESSAGE = "User not authorised to operation";
    }

    public static class ProductName {
        public static final String INVESTMENT = "ISK";
    }

    public static class ErrorCodes {
        // token required
        public static final String TOKEN_REQUIRED = "token_required";

        public static final String AGREEMENT_CONFLICT = "agreement_conflict";

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
        public static final String AUTH_NOT_STARTED = "start_failed";
        public static final String CHALLENGE_EXPIRED = "challenge_expired";
        public static final String RESOURCE_NOT_FOUND = "resource_not_found";
        public static final String AUTHENTICATION_COLLISION = "authentication_collision";
        public static final String AUTHENTICATION_FAILED = "authentication_failed";
        public static final String UNABLE_TO_FETCH_ACCOUNTS = "Could not retrieve accounts.";
        public static final String DUPLICATE_PAYMENT =
                "Duplicate payment. Technical code. Please try again.";
        public static final String TRANSFER_REJECTED = "Transfer rejected by Nordea";
        public static final String PAYMENT_ERROR = "Something went wrong with the payment.";
        public static final String UNREGISTERED_RECIPIENT =
                "Recipient accounts missing from accounts ledger";
        public static final String NOT_ENOUGH_FUNDS = "Not enough funds";
        public static final String EXTERNAL_SERVICE_CALL_FAILED = "External service call failed";
        public static final String INTERNAL_SERVER_ERROR = "internal_server_error";
        public static final String INTERNAL_SERVER_ERROR_MESSAGE =
                "Something went wrong. Please try again later.".toLowerCase();
        public static final String SIGNING_COLLISION = "signing_collision";
        public static final String SIGNING_COLLISION_MESSAGE = "A signing collision has occurred.";
        public static final String WRONG_TO_ACCOUNT_LENGTH = "BESE1125";
        public static final String WRONG_TO_ACCOUNT_LENGHT_MESSAGE =
                "Wrong To account length for the chosen bank";
        public static final String REMOTE_SERVICE_ERROR = "unknown_execution_error_remote_service";
        public static final String REMOTE_SERVICE_ERROR_MESSAGE =
                "An exception has occurred when invoking remote service.".toLowerCase();
        public static final String HYSTRIX_CIRCUIT_SHORT_CIRCUITED =
                "Hystrix circuit short".toLowerCase();
        public static final String TIMEOUT_AFTER_MESSAGE = "Timeout after".toLowerCase();
        public static final String ERROR_CORE_UNKNOWN = "error_core_unknown";
        public static final String INVALID_PARAMETERS_FOR_PAYMENT =
                "Invalid parameter(s) for payment";
        public static final String BESE1076 = "BESE1076".toLowerCase();
        public static final String INVALID_OCR_ERROR_CODE = "BESE1009";
        public static final String OWN_MESSAGE_CONSTRAINTS =
                "Own message must be between".toLowerCase();
        public static final String UNEXPECTED_EXECUTION_ERROR_CODE = "unexpected_execution_error";
        public static final String UNEXPECTED_EXECUTION_ERROR =
                "An unexpected execution error has occurred".toLowerCase();
        public static final String USER_UNAUTHORIZED = "error_core_unauthorized";
        public static final String USER_UNAUTHORIZED_MESSAGE = "User not authorised to operation";
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
