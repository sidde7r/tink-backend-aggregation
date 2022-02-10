package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank;

import com.google.common.collect.ImmutableList;
import java.time.ZoneId;
import java.util.List;
import java.util.regex.Pattern;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccountTypeMapper;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.account.enums.AccountFlag;
import se.tink.libraries.i18n_aggregation.LocalizableEnum;
import se.tink.libraries.i18n_aggregation.LocalizableKey;

public final class SwedbankConstants {

    private SwedbankConstants() {
        throw new AssertionError();
    }

    public static final String INTEGRATION_NAME = "swedbank";
    public static final String SWEDBANK_OB_PROVIDER_NAME = "se-swedbank-ob";
    public static final String SWEDBANK_OB_BUSINESS_PROVIDER_NAME = "se-swedbank-business-ob";
    public static final ZoneId ZONE_ID = ZoneId.of("Europe/Stockholm");

    public static class Format {
        public static final String TRANSACTION_DESCRIPTION_SEPARATOR = " ";
    }

    public static class Urls {
        public static final String BASE = "https://psd2.api.swedbank.com";

        public static final URL AUTHORIZATION_DECOUPLED =
                new URL(BASE.concat(Endpoints.AUTHORIZATION_DECOUPLED));
        public static final URL TOKEN = new URL(BASE.concat(Endpoints.TOKEN));
        public static final URL ACCOUNTS = new URL(BASE.concat(Endpoints.ACCOUNTS));
        public static final URL ACCOUNT_BALANCES = new URL(BASE.concat(Endpoints.ACCOUNT_BALANCES));
        public static final URL ACCOUNT_TRANSACTIONS =
                new URL(BASE.concat(Endpoints.ACCOUNT_TRANSACTIONS));
        public static final URL CONSENTS = new URL(BASE.concat(Endpoints.CONSENTS));
        public static final URL CONSENT_STATUS = new URL(BASE.concat(Endpoints.CONSENT_STATUS));
        public static final URL INITIATE_PAYMENT = new URL(BASE + Endpoints.INITIATE_PAYMENT);
        public static final URL GET_PAYMENT = new URL(BASE + Endpoints.GET_PAYMENT);
        public static final URL GET_PAYMENT_STATUS = new URL(BASE + Endpoints.GET_PAYMENT_STATUS);
        public static final URL INITIATE_PAYMENT_AUTH =
                new URL(BASE + Endpoints.INITIATE_PAYMENT_AUTH);
        public static final URL DELETE_PAYMENT = new URL(BASE + Endpoints.DELETE_PAYMENT);
    }

    public static class Endpoints {
        public static final String AUTHORIZATION_DECOUPLED = "/psd2/v4/authorize-decoupled";
        public static final String TOKEN = "/psd2/token";
        public static final String ACCOUNTS = "/v4/accounts";
        public static final String ACCOUNT_BALANCES = "/v4/accounts/{account-id}/balances";
        public static final String ACCOUNT_TRANSACTIONS = "/v4/accounts/{account-id}/transactions";
        public static final String CONSENTS = "/v4/consents";
        public static final String CONSENT_STATUS = CONSENTS + "/{consent-id}/status";
        public static final String INITIATE_PAYMENT = "/v3/payments/{paymentType}";
        public static final String GET_PAYMENT = "/v3/payments/{paymentType}/{paymentId}";
        public static final String GET_PAYMENT_STATUS =
                "/v3/payments/{paymentType}/{paymentId}/status";
        public static final String INITIATE_PAYMENT_AUTH =
                "/v3/payments/{paymentType}/{paymentId}/authorisations";
        public static final String DELETE_PAYMENT = "/v3/payments/{paymentType}/{paymentId}";
    }

    public static class UrlParameters {
        public static final String ACCOUNT_ID = "account-id";
        public static final String CONSENT_ID = "consent-id";
        public static final String PAYMENT_TYPE = "paymentType";
        public static final String PAYMENT_ID = "paymentId";
    }

    public static class StorageKeys {
        public static final String ACCOUNT_ID = "ACCOUNT_ID";
        public static final String CONSENT = "CONSENT";
        public static final String HOLDER_NAME = "HOLDER_NAME";
        public static final String CONSENT_TRANSACTIONS_OVER_90_DAYS =
                "CONSENT_TRANSACTIONS_OVER_90_DAYS";
    }

    public static class QueryKeys {
        public static final String CLIENT_ID = "client_id";
        public static final String APP_ID = "app-id";
        public static final String SCOPE = "scope";
        public static final String BIC = "bic";
        public static final String REDIRECT_URI = "redirect_uri";
        public static final String STATE = "state";
        public static final String RESPONSE_TYPE = "response_type";
        public static final String CLIENT_SECRET = "client_secret";
        public static final String CODE = "code";
        public static final String GRANT_TYPE = "grant_type";
        public static final String REFRESH_TOKEN = "refresh_token";
        public static final String BOOKING_STATUS = "bookingStatus";
    }

    public static class QueryValues {
        public static final String RESPONSE_TYPE_CODE = "code";
        public static final String GRANT_TYPE_CODE = "authorization_code";
        public static final String GRANT_TYPE_REFRESH_TOKEN = "refresh_token";
        public static final String BOOKING_STATUS_BOTH = "both";
        public static final String BOOKING_STATUS_BOOKED = "booked";
    }

    public static class HeaderKeys {
        // X_REQUEST_ID may need to be lowercase for PIS related requests. According to docs
        // the casing for the header should be as below, if this causes issues for PIS a new
        // constant should be created.
        public static final String X_REQUEST_ID = "X-Request-ID";
        public static final String CONSENT_ID = "Consent-ID";
        public static final String FROM_DATE = "dateFrom";
        public static final String TO_DATE = "dateTo";
        public static final String TPP_REDIRECT_URI = "TPP-Redirect-URI";
        public static final String TPP_NOK_REDIRECT_URI = "TPP-Nok-Redirect-URI";
        public static final String PSU_IP_ADDRESS = "PSU-IP-Address";
        public static final String PSU_IP_PORT = "PSU-IP-Port";
        public static final String PSU_USER_AGENT = "PSU-User-Agent";
        public static final String PSU_HTTP_METHOD = "PSU-HTTP-Method";
        public static final String ACCEPT = "accept";
        public static final String PSU_ID = "PSU-ID";
        public static final String DIGEST = "digest";
        public static final String SIGNATURE = "signature";
        public static final String TPP_SIGNATURE_CERTIFICATE = "tpp-signature-certificate";
        public static final String DATE = "date";
        public static final String TPP_REDIRECT_PREFERRED = "TPP-Redirect-Preferred";
    }

    public static class HeaderValues {
        public static final String PSU_IP_ADDRESS = "127.0.0.1";
        public static final String PSU_IP_PORT = "443";
        public static final String PSU_HTTP_METHOD = "GET";
        public static final String PSU_USER_AGENT = "Tink";
        public static final String SIGNATURE_HEADER =
                "keyId=\"%s\",algorithm=\"rsa-sha256\",headers=\"%s\",signature=\"%s\"";
        public static final String DATE_PATTERN = "EEE, dd MMM yyyy HH:mm:ss zzz";
        public static final String TPP_REDIRECT_PREFERRED = "false";
    }

    public static class RequestValues {
        public static final String PSD2 = "PSD2";
        public static final String MOBILE_ID = "MOBILE_ID";
        public static final String SMART_ID = "SMART_ID";
        public static final String ALL_ACCOUNTS_SCOPES = "PSD2 PSD2account_list";
    }

    public static class BICProduction {
        public static final String SWEDEN = "SWEDSESS";
        public static final String ESTONIA = "HABAEE2X";
        public static final String LITHUANIA = "HABALT22";
        public static final String LATVIA = "HABALV22";
    }

    public static class AuthStatus {
        public static final String RECEIVED = "received";
        public static final String STARTED = "started";
        public static final String FINALIZED = "finalised";
        public static final String FAILED = "failed";
        public static final String EMPTY = "";
    }

    public static class ErrorMessages {
        public static final String INVALID_CONFIGURATION =
                "Invalid Configuration: %s cannot be empty or null";
        public static final String MISSING_CONFIGURATION = "Client Configuration missing.";
        public static final String MISSING_TOKEN = "Cannot find token.";
        public static final String SCA_REQUIRED = "sca_required";
        public static final String TRANSACTION_SCA_ALREADY_SIGNED =
                "transactions resource has already been signed";
        public static final String INVALID_ACCOUNT_TYPE = "Invalid account type: %s";
        public static final String INVALID_PAYMENT_TYPE =
                "No SwedbankPaymentType found for AccountIdentifiers pair %s";
        public static final String INVALID_REMITTANCE_INFORMATION_VALUE =
                "Invalid destination message.";
        public static final String ACTIVATE_EXTENDED_BANKID =
                "To add a new recipient, activate Mobile BankID for extended use.";
        public static final String AUTHORIZATION_FAILED = "authorization_failed";
        public static final String UNKNOWN_AUTHORIZATION_ID = "Unknown authorizationId";
        public static final String ERROR_INVESTIGATION_MESSAGE =
                "It's expected to be valid, this needs to be investigated.";
    }

    public static class ConsentStatus {
        public static final String VALID = "valid";
        public static final String SIGNED = "signed";
        public static final String REVOKED_BY_PSU = "revokedbypsu";
        public static final String EXPIRED = "expired";
        public static final String TERMINATED_BY_TPP = "terminatedbytpp";
    }

    public static final class BodyParameter {
        public static final String ALL_ACCOUNTS = "allAccounts";
        public static final int FREQUENCY_PER_DAY = 4;
        public static final int FREQUENCY_PER_DAY_ALL_ACCOUNTS = 1;
        public static final boolean RECURRING_INDICATOR = false;
        public static final boolean COMBINED_SERVICE_INDICATOR = false;
    }

    public static final class TimeValues {
        public static final int SLEEP_TIME_MILLISECONDS = 4000;
        public static final int MONTHS_TO_FETCH_MAX = 25;
        public static final int DAYS_TO_FETCH_HOLDER_NAME = 30;
        public static final int ONLINE_STATEMENT_MAX_DAYS = 89;
        public static final int ATTEMPS_BEFORE_TIMEOUT = 10;
        public static final int CONSENT_DURATION_IN_DAYS = 90;
        public static final int RETRY_TRANSACTIONS_DOWNLOAD = 5000;

        public static final long SCA_STATUS_POLL_DELAY = 3000;
        public static final int SCA_STATUS_POLL_FREQUENCY = 2000;
        public static final int SCA_STATUS_POLL_MAX_ATTEMPTS = 90;
    }

    public static final class LogMessages {
        public static final String TRANSACTION_SIGNING_TIMED_OUT =
                "Sign for fetching transactions for the last 25 months, timed out";
    }

    public static final class HttpStatus {
        public static final int RESOURCE_PENDING = 428;
        public static final int ACCESS_EXCEEDED = 429;
    }

    public static final class ErrorCodes {
        public static final String KYC_INVALID = "KYC_INVALID";
        public static final String SCA_REQUIRED = "SCA_REQUIRED";
        public static final String REFRESH_TOKEN_EXPIRED = "Provided refresh_token expired";
        public static final String WRONG_USER_ID = "Wrong UserId parameter";
        public static final String NO_PROFILE_AVAILABLE = "No profile available";
        public static final String LOGIN_SESSION_INTERRUPTED = "Other login session is ongoing";
        public static final String EMPTY_USER_ID = "Mandatory header value is empty: PSU-ID";
        public static final String AUTHORIZATION_EXPIRED = "Authorization expired";
        public static final String CONSENT_INVALID = "CONSENT_INVALID";
        public static final String CONSENT_EXPIRED = "CONSENT_EXPIRED";
        public static final String RESOURCE_NOT_FOUND = "RESOURCE_NOT_FOUND";
        public static final String USER_INTERRUPTION = "USER_INTERRUPTION";
        public static final String MOBILE_ID_EXCEPTION = "MOBILE_ID_EXCEPTION";
        public static final String FORMAT_ERROR = "FORMAT_ERROR";
        public static final String INSUFFICIENT_FUNDS = "INSUFFICIENT_FUNDS";
        public static final String INVALID_RECIPIENT = "INVALID_RECIPIENT";
        public static final String MISSING_CT_AGREEMENT = "MISSING_CT_AGREEMENT";
        public static final String INVALID_AUTH_TOKEN = "TOKEN_INVALID";
        public static final String RESOURCE_ALREADY_SIGNED = "RESOURCE_ALREADY_SIGNED";
        public static final String MISSING_BANK_ID = "MISSING_BANK_ID";
        public static final String INTERNET_BANK_AGREEMENT = "INTERNET_BANK_AGREEMENT";
    }

    public static final class AccountIdentifierPrefix {
        public static final String PERSONAL_ACCOUNT = "PA ";
        public static final String BANK_GIRO = "BG ";
        public static final String PLUS_GIRO = "PG ";
    }

    public static final class ReferenceType {
        public static final String OCR = "OCR";
        public static final String MSG = "MSG";
    }

    public static final class MarketCode {
        public static final String SE = "SE";
        public static final String LV = "LV";
        public static final String LT = "LT";
        public static final String EE = "EE";
    }

    public static final class BalanceType {
        public static final String BOOKED = "interimBooked";
        public static final String AVAILABLE = "interimAvailable";
        public static final String AUTHORISED = "authorised";
    }

    public static final class Transactions {
        public static final Pattern SALARY_PATTERN = Pattern.compile(".*(l[oö]n|salary).*");
    }

    public enum HeadersToSign {
        X_REQUEST_ID("x-request-id"),
        TPP_REDIRECT_URI("tpp-redirect-uri"),
        DATE(HeaderKeys.DATE),
        DIGEST(HeaderKeys.DIGEST);
        private String header;

        HeadersToSign(String header) {
            this.header = header;
        }

        public String getHeader() {
            return header;
        }
    }

    public enum EndUserMessage implements LocalizableEnum {
        MUST_UPDATE_AGREEMENT(
                new LocalizableKey(
                        "To be able to refresh your accounts you need to answer some questions from your bank. Please log in to your bank's app or internet bank.")),
        WRONG_BANK_SWEDBANK(
                new LocalizableKey(
                        "You do not have any accounts at Swedbank. Use Sparbankerna (Mobile BankID) instead.")),
        WRONG_BANK_SAVINGSBANK(
                new LocalizableKey(
                        "You do not have any accounts at Sparbankerna. Use Swedbank (Mobile BankID) instead.")),
        ACTIVATE_EXTENDED_BANKID(
                new LocalizableKey(
                        "In order to add new recipients you need to activate Mobile BankID for extended use. You can find more information on how to extend your Mobile BankID in your Internet bank.")),
        UNKNOWN_ERROR(new LocalizableKey("Unknown error"));

        private final LocalizableKey userMessage;

        EndUserMessage(LocalizableKey userMessage) {
            this.userMessage = userMessage;
        }

        @Override
        public LocalizableKey getKey() {
            return this.userMessage;
        }
    }

    public static final List<String> BALTICS = ImmutableList.of("LV", "LT", "EE");

    public static final TransactionalAccountTypeMapper ACCOUNT_TYPE_MAPPER =
            TransactionalAccountTypeMapper.builder()
                    .put(
                            TransactionalAccountType.CHECKING,
                            AccountFlag.PSD2_PAYMENT_ACCOUNT,
                            "Bokonto",
                            "Företagskonto",
                            "Lönekonto",
                            "Personalkonto",
                            "Privatkonto",
                            "Servicekonto",
                            "Transaktionskonto",
                            "Ungdomskonto",
                            "Valutakonto",
                            "Current",
                            "Limit",
                            "Limit_revolving",
                            "Privatkonto kombi")
                    // TODO: check savings account for new types for Baltics
                    .put(
                            TransactionalAccountType.SAVINGS,
                            "Depåkonto 1",
                            "Framtidskonto",
                            "HSB Bosparkonto",
                            "Kapitalkonto",
                            "Placeringskonto ftg",
                            "Premiumkonto",
                            "Private Banking",
                            "Skogslikvidkonto",
                            "Spara&Bokonto",
                            "Sparkapitalkonto",
                            "Specialkonto Pri",
                            "Världsnaturkonto",
                            "e-sparkonto")
                    .build();

    // First one is Swedbank, the rest are Sparbanken bank IDs
    public static final List<String> BANK_IDS =
            ImmutableList.of(
                    "08999", "08191", "08024", "08234", "08050", "08060", "08336", "08129", "08331",
                    "08138", "08143", "08158", "08183", "08164", "08187", "08393", "08217", "08222",
                    "08240", "08353", "08242", "08284", "08286", "08295", "08239", "08304", "08059",
                    "08006", "08403", "08305", "08153", "08314", "08264", "08270", "08032", "08289",
                    "08313", "08351", "08464", "08182", "08114", "08346", "08321", "08257", "08354",
                    "08422", "08356", "08380", "08383", "08384", "08388", "08401", "08405", "08431",
                    "08434", "08440", "08021", "08030", "08055");
}
