package se.tink.backend.aggregation.agents.nxgen.be.banks.kbc;

import com.google.common.base.Preconditions;
import java.util.Locale;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.utils.log.LogTag;
import se.tink.backend.aggregation.nxgen.core.account.TypeMapper;
import se.tink.backend.aggregation.nxgen.http.UrlEnum;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.i18n.LocalizableEnum;
import se.tink.libraries.i18n.LocalizableKey;
import se.tink.libraries.i18n.LocalizableParametrizedEnum;
import se.tink.libraries.i18n.LocalizableParametrizedKey;

public class KbcConstants {

    public static final String LANGUAGE_DUTCH = "nl";
    public static final String DEFAULT_LANGUAGE_FOR_PARSE_ERROR_TEXTS =
            Locale.ENGLISH.getLanguage();

    public enum Url implements UrlEnum {
        KEY_EXCHANGE(createUrlWithHost("/SAI/A054/service/keyExchange/1")),
        CHALLENGE(createUrlWithHost("/SAI/A054/service/challenge/1")),
        REGISTER_LOGON(createUrlWithHost("/SAI/A054/service/logon/ucr/1")),
        ENROLL_DEVICE(createUrlWithHost("/SAI/A052/service/enrollDevice/2")),
        SIGNING_TYPES(createUrlWithHost("/SAI/A052/service/signing/types/1")),
        SIGNING_CHALLENGE(createUrlWithHost("/SAI/A052/service/signing/challenge/ucr/1")),
        SIGNING_VALIDATION(createUrlWithHost("/SAI/A052/service/signing/validation/ucr/1")),
        ACTIVATION_LICENSE(createUrlWithHost("/SAI/A054/service/activation/license/1")),
        ACTIVATION_INSTANCE(createUrlWithHost("/SAI/A054/service/activation/instance/1")),
        ACTIVATION_VERIFICATION(createUrlWithHost("/SAI/A054/service/activation/verification/1")),
        CHALLENGE_SOTP(createUrlWithHost("/SAI/A054/service/challenge/sotp/1")),
        LOGIN_SOTP(createUrlWithHost("/SAI/A054/service/login/sotp/1")),
        LOGOUT(createUrlWithHost("/SAI/A054/service/logout/1")),
        ACCOUNTS(createUrlWithHost("/MOB/A031/accounts/dashboard/1")),
        TRANSACTIONS_HISTORY(createUrlWithHost("/MOB/A031/transactions/history/1")),
        CARDS(createUrlWithHost("/MOB/A031/cards/1")),
        INVESTMENT_PLAN_OVERVIEW(createUrlWithHost("/MOB/A031/investment-plan/bank/overview/1")),
        INVESTMENT_PLAN_DETAIL(createUrlWithHost("/MOB/A031/investment-plan/bank/detail/1")),
        ASSETS(createUrlWithHost("/MOB/A031/saveInvest/assets/1")),
        ASSETS_DETAIL(createUrlWithHost("/MOB/A031/saveInvest/assets/detail/1")),
        FUTURE_TRANSACTIONS(createUrlWithHost("/MOB/A031/future-transactions/1")),
        ACCOUNTS_FOR_TRANSFER_TO_OWN(createUrlWithHost("/MOB/A031/accounts/for-transfer-to/own/1")),
        ACCOUNTS_FOR_TRANSFER_TO_OTHER(
                createUrlWithHost("/MOB/A031/accounts/for-transfer-to/other/1")),
        BENEFICIARIES_HISTORY(createUrlWithHost("/MOB/A031/beneficiaries/history/1")),
        TRANSFER_VALIDATE(createUrlWithHost("/MOB/A031/transfer/validate/1")),
        TRANSFER_TO_OTHER(createUrlWithHost("/MOB/A031/transfer/other/1")),
        TRANSFER_TO_OWN(createUrlWithHost("/MOB/A031/transfer/own/1")),
        TRANSFER_TO_OWN_INSTANT(createUrlWithHost("/MOB/A031/instant-transfer/own/1")),
        TRANSFER_TO_OTHER_INSTANT(createUrlWithHost("/MOB/A031/instant-transfer/other/1")),
        MOB_A031_SIGNING_TYPES(createUrlWithHost("/MOB/A031/signing/types/1")),
        MOB_A031_SIGNING_CHALLENGE_SOTP(createUrlWithHost("/MOB/A031/signing/challenge/sotp/1")),
        MOB_A031_SIGNING_VALIDATION_SOTP(createUrlWithHost("/MOB/A031/signing/validation/sotp/1")),
        MOB_A031_SIGNING_CHALLENGE_UCR(createUrlWithHost("/MOB/A031/signing/challenge/ucr/1")),
        MOB_A031_SIGNING_VALIDATION_UCR(createUrlWithHost("/MOB/A031/signing/validation/ucr/1"));

        private URL url;

        Url(String url) {
            this.url = new URL(url);
        }

        @Override
        public URL get() {
            return url.queryParam("version", VERSION);
        }

        @Override
        public URL parameter(String key, String value) {
            return url.parameter(key, value);
        }

        @Override
        public URL queryParam(String key, String value) {
            return url.queryParam(key, value);
        }

        public static final String HOST = "https://mobile.kbc-group.com";
        public static final String VERSION = "1908";

        private static String createUrlWithHost(String uri) {
            return HOST + uri;
        }
    }

    public static class ApplicationId {
        public static final String REGISTER_LOGON = "A031";
        public static final String APPLICATION_TYPE_CODE = "00C";
    }

    public static class RequestInput {
        public static final String APP_FAMILY = "PHNIOSV1";
        // At the time of writing KBC seems to implement their version check as '> {current}'
        // This means that v.99.9.9 is considered supported, which should allow us to not have to
        // update it too often.
        public static final String VERSION_NUMBER = "99.9.9";
        public static final String APPLICATION_ID = "A031";
        public static final String COMPANY_ID = "0001";

        // == Start UCR Challenge ==
        public static final String AUTHENTICATION_TYPE = "UCR_PLUS";
        // == End UCR Challenge ==

        // == Start Register UCR logon ==
        public static final boolean SAVE_CARD_NUMBER = false;
        public static final String EMPTY_CAPTCHA = "";
        public static final String UCR_TYPE = "UCR_PLUS_MANUAL";
        // == End Register UCR logon ==

        // == Start Enroll device ==
        public static final boolean IS_POLITICAL_PROMINENT_PERSION_CHECKED = false;
        public static final boolean IS_COMMON_REPOST_STANDARD_CHECKED = false;
        public static final boolean APP_CONDITIONS_CHECKED = true;
        public static final boolean BANK_CONDITIONS_CHECKED = true;
        public static final boolean DOCCLE_CHECKED = false;
        // == End Enroll device ==

        public static final String OS_VERSION_NO = "11.2.1";
        public static final String OS_TYPE = "IOS";

        public static final String DEVICE_NAME = "iPhone9,3";
        public static final String WITH_TOUCH_ID = "false";

        public static final String CURRENCY = "EUR";
        public static final String SEARCH_MESSAGE = "";
        public static final String SEARCH_AMOUNT = "";
        public static final int TRANSACTIONS_QUANTITY = 15;
    }

    public static class Headers {
        public static final String X_XSRF_TOKEN = "X-XSRF-TOKEN";

        public static final String USER_AGENT_VALUE = "KBC iPhone " + RequestInput.VERSION_NUMBER;

        public static final String COMPANY_KEY = "company";
        public static final String COMPANY_VALUE = RequestInput.COMPANY_ID;
        public static final String APPVERSION_KEY = "appVersion";
        public static final String APPVERSION_VALUE = RequestInput.VERSION_NUMBER;
        public static final String ACCEPT_LANG_KEY = "Accept-Language";
    }

    public static class Predicates {
        public static final String XSRF_TOKEN = "XSRF-TOKEN";
        public static final String SIGN_TYPE_MANUAL = "UCR_PLUS_SIGN_MANUAL";
        public static final String SIGN_TYPE_SOTP = "SOTP";
    }

    public static class ResultCode {
        public static final String DOUBLE_ZERO = "00";
        public static final String ZERO_TWO = "02";
        public static final String ZERO_NINE = "09"; // isSigningRequired
    }

    public static class ErrorCodes {
        public static final String AUTHENTICATION_ERROR = "02";
        public static final String INVALID_SIGN_CODE_LAST_TRY = "10";
    }

    public static class PairTypeTypes {
        public static final String BOOLEAN = "boolean";
        public static final String TEXT = "text";
        public static final String HIDDEN = "hidden";
        public static final String IBAN = "ibanbban";
        public static final String SHORT = "short";
        public static final String DECIMAL = "decimal";
        public static final String DATE = "date";
        public static final String REFERENCE_STRUCTURED = "tsfr";
    }

    public static class Encryption {
        // == Start Shared stuff ==
        public static final int AES_KEY_LENGTH = 16;
        public static final int IV_LENGTH = 16;
        public static final int FINGERPRINT_LENGTH = 50;
        // == End Shared stuff ==
        // == Start Device enrollment ==
        public static final String PUBLIC_KEY =
                "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCbdfKIRPYp399wiG0Acu8d94x5JwCjU5"
                        + "2jEPco2pQgHcZSxE0k2nqrJmb393Wxyfr43hfdWtI4Le+beIsiNt32hTnejtprTwE94qAikTq3xO3BmTS/xZGPyWygD9QVDafYF3"
                        + "VTk3R50Ej1cBl1t1vC8fh4dl2nWKjUHSZOwup3EQIDAQAB";
        public static final String AES_SESSION_KEY_KEY = "aesSessionKey";

        // == End Device enrollment ==
        // == Start Service Activation ==
        public static final int ITERATIONS = 1024;
        public static final byte[] SALT = "0".getBytes();
        // == End Service Activation ==
    }

    public static class Storage {
        public static final String DEVICE_KEY = "device";
        public static final String SIGNING_ID = "SIGNING_ID";
    }

    public static class LogTags {
        public static final LogTag ERROR_CODE_MESSAGE = LogTag.from("#be_kbc_error_message");
    }

    public static final String[] IGNORED_ACCOUNT_TYPES = {
        "0029", // KBC-Derdenrekening
        "0038", // KBC-Rubriekrekening
        "1013", // KBC-Beleggersrekening
        "2117", // KBC-Pandrekening
        "3123", // ESOP-rekening
        "0346", // KBC-Vermogensrekening
        "3465", // KBC-Business Comfortrekening
        "3637", // KBC-Business Compactrekening
        "3774", // Compte épargne Call32 corporate KBC
        "4012", // KBC Brussels Security Deposit Account
        "4057", // KBC Security Deposit Account
        "4058"
    }; // Compte d'épargne gar.locative KBC Brussels

    public static final TypeMapper<AccountTypes> ACCOUNT_TYPE_MAPPER =
            TypeMapper.<AccountTypes>builder()
                    .put(
                            AccountTypes.CHECKING,
                            "0028", // Company Card
                            "0030", //
                            "3737", // Business Pro
                            "3844") // KBC-Zichtrekening
                    .put(
                            AccountTypes.SAVINGS,
                            "3590", // Start2Save
                            "3591",
                            "3594", // KBC-Huurwaarborgspaarrekening
                            "3595", // KBC Tall Oaks Savings Account
                            "3614", // KBC-Spaarrekening
                            "3781", // KBC Start2Save4
                            "3866", // KBC-Spaarrekening PLUS
                            "3867", // KBC Savings Pro
                            "4010", // KBC Brussels-Start2Save
                            "4011", // KBC Brussels Savings Account
                            "4013", // KBC Brussels-Groeispaarrekening
                            "4019", // KBC Brussels Savings Account PRO
                            "4056") // KBC-Huurwaarborgspaarrekening
                    .ignoreKeys(IGNORED_ACCOUNT_TYPES)
                    .build();

    public static final class ErrorMessage {
        // Probably safe to remove we have HeaderErrorMessage
        public static final String INCORRECT_CARD_NUMBER =
                "the card number you have entered is incorrect";
        public static final String INCORRECT_LOGIN_CODE = "you have entered the wrong login code";

        // Hoping the logging will log HeaderErrorMessage that can be used as replacement
        public static final String NOT_A_CUSTOMER = "managing branch not found";
        public static final String ACCOUNT_BLOCKED =
                "type in the characters as they appear in the image below and then click 'confirm'";
        public static final String ACCOUNT_BLOCKED2 = "your pin has been blocked";
        public static final String INCORRECT_SIGN_CODE = "your sign code is incorrect";

        // As of 18.10 only text messages and the generic error code 02
        public static final String NO_TRANSACTIONS_FOUND =
                "no transactions in the most recent 12 months";
        public static final String NO_TRANSACTIONS_FOUND_NL =
                "geen verrichtingen in de recentste 12 maanden";
        public static final String NO_TRANSACTIONS_FOUND_FR =
                "aucune opération les 12 derniers mois";
        public static final String NO_TRANSACTIONS_FOUND_DE =
                "keine transaktionen in den letzten 12 monaten";
        // Only text message no header or code, we do transfers in English to ensure that
        // correctness
        public static final String ACCOUNT_HAS_INSUFFICIENT_FUNDS = "account has no funds";
        //  IBM Security Access Manager Manual: Third party server error or network problem
        public static final String THIRD_PARTY_SERVER_ERROR = "0x38cf04d7";
    }

    public static final class HeaderErrorMessage {
        public static final String INCORRECT_LOGIN_CODE_TWO_ATTEMPT_LEFT = "D9FE50";
        public static final String INCORRECT_LOGIN_CODE_ONE_ATTEMPT_LEFT = "D9E028";
        public static final String INCORRECT_CARD_NUMBER = "D93058";
        public static final String[] CANNOT_LOGIN_USING_THIS_CARD_CONTACT_KBC = {
            "D9FE51", "D93060"
        };
    }

    public enum UserMessage implements LocalizableEnum {
        INCORRECT_CARD_NUMBER(
                new LocalizableKey(
                        "The card number you have entered is incorrect. Please try again.")),
        NOT_A_CUSTOMER(new LocalizableKey("The provided credentials are not for KBC."));

        private LocalizableKey userMessage;

        UserMessage(LocalizableKey userMessage) {
            this.userMessage = userMessage;
        }

        @Override
        public LocalizableKey getKey() {
            return userMessage;
        }
    }

    public enum TransferMessage implements LocalizableEnum {
        MISSING_SOURCE_NAME(new LocalizableKey("Originator name must be specified.")),
        MISSING_DESTINATION_NAME(new LocalizableKey("Beneficiary name must be specified.")),
        DUE_DATE_TOO_FAR_IN_FUTURE(new LocalizableKey("Due date must be within a year from today"));

        private LocalizableKey userMessage;

        TransferMessage(LocalizableKey userMessage) {
            this.userMessage = userMessage;
        }

        @Override
        public LocalizableKey getKey() {
            return userMessage;
        }
    }

    public enum TransferMessageParametrized implements LocalizableParametrizedEnum {
        MSG_LENGTH_EXCEEDS_MAX(
                new LocalizableParametrizedKey("Reference must be max {} characters.")),
        AMOUNT_LESS_THAN_MIN(
                new LocalizableParametrizedKey("Transfer amount can't be less than {}."));

        private final LocalizableParametrizedKey key;

        TransferMessageParametrized(LocalizableParametrizedKey key) {
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

    public static class Transfers {
        public static final double MIN_AMOUNT = 0.01;
        public static final int MAX_MSG_LENGTH = 70;
        public static final String TRANSFER_TYPE_CODE = "0";
        public static final String SCASH_VERSION_NUMBER = "";
        public static final String TRANSFER_TO_OWN_ACCOUNT = "transferOwnAccount";
        public static final String TRANSFER_TO_OTHER_ACCOUNT = "transferOtherAccount";
    }

    public static class ErrorHeaders {
        public static final String LOGON_ERROR = "logon-error-code";
    }

    public static class Investments {
        public static final String LEFT_TO_INVEST = "left to invest";
        public static final String INVESTMENTS = "investments";
    }

    static class HttpClient {
        public static final int MAX_RETRIES = 4;
        public static final int RETRY_SLEEP_MILLISECONDS = 1000;
    }
}
