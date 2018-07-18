package se.tink.backend.aggregation.agents.nxgen.be.banks.kbc;

import com.google.common.collect.ImmutableMap;
import se.tink.backend.aggregation.agents.utils.log.LogTag;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.http.UrlEnum;
import se.tink.backend.aggregation.rpc.AccountTypes;
import se.tink.libraries.i18n.LocalizableEnum;
import se.tink.libraries.i18n.LocalizableKey;

public class KbcConstants {
    public static final String LANGUAGE = "en";

    public enum Url implements UrlEnum {
        KEY_EXCHANGE(createUrlWithHost("/SAI/A054/service/keyExchange/1")),
        CHALLENGE(createUrlWithHost("/SAI/A054/service/challenge/1")),
        REGISTER_LOGON(createUrlWithHost("/SAI/A054/service/logon/ucr/1")),
        PERSONALISATION(createUrlWithHost("/SAI/A052/service/personalisation/1")),
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
        ACCOUNTS(createUrlWithHost("/MOB/A031/accounts/2")),
        TRANSACTIONS_HISTORY(createUrlWithHost("/MOB/A031/transactions/history/1")),
        CARDS(createUrlWithHost("/MOB/A031/cards/3")),
        FUTURE_TRANSACTIONS(createUrlWithHost("/MOB/A031/future-transactions/2")),
        ACCOUNTS_FOR_TRANSFER_TO_OWN(createUrlWithHost("/MOB/A031/accounts/for-transfer-to/own/2")),
        ACCOUNTS_FOR_TRANSFER_TO_OTHER(createUrlWithHost("/MOB/A031/accounts/for-transfer-to/other/2")),
        BENEFICIARIES_HISTORY(createUrlWithHost("/MOB/A031/beneficiaries/history/1")),
        TRANSFER_VALIDATE(createUrlWithHost("/MOB/A031/transfer/validate/1")),
        TRANSFER_TO_OTHER(createUrlWithHost("/MOB/A031/transfer/other/1")),
        TRANSFER_TO_OWN(createUrlWithHost("/MOB/A031/transfer/own/1")),
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
        public static final String VERSION = "1805";

        private static String createUrlWithHost(String uri) {
            return HOST + uri;
        }
    }

    public static final class MultiFactorAuthentication {
        public static final String CODE = "code";
    }

    public static class ApplicationId {
        public static final String REGISTER_LOGON = "A031";
        public static final String APPLICATION_TYPE_CODE = "00C";
    }

    public static class RequestInput {
        public static final String APP_FAMILY = "PHNIOSV1";
        public static final String VERSION_NUMBER = "18.5.0";
        public static final String APPLICATION_ID = "3700";
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

        public static final String OS_VERSION_NO = "10.1.1";
        public static final String OS_TYPE = "IOS";

        public static final String DEVICE_NAME = "iPhone9,3";
        public static final String WITH_TOUCH_ID = "false";

        public static final String CURRENCY = "EUR";
        public static final String SEARCH_MESSAGE = "";
        public static final String SEARCH_AMOUNT = "";
        public static final String ROLE_CODE = "T";
        public static final int TRANSACTIONS_QUANTITY = 15;
    }

    public static class Headers {
        public static final String X_XSRF_TOKEN = "X-XSRF-TOKEN";

        public static final String USER_AGENT_VALUE = "KBC iPhone " + RequestInput.VERSION_NUMBER;
    }

    public static class Predicates {
        public static final String XSRF_TOKEN = "XSRF-TOKEN";
        public static final String SIGN_TYPE_MANUAL = "UCR_PLUS_SIGN_MANUAL";
        public static final String SIGN_TYPE_SOTP = "SOTP";
    }

    public static class ResultCode {
        public static final String DOUBLE_ZERO = "00";
        public static final String ZERO_TWO = "02";
        public static final String ZERO_NINE = "09";
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
        public static final String PUBLIC_KEY = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCbdfKIRPYp399wiG0Acu8d94x5JwCjU5"
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
    }

    public static class LogTags {
        public static final LogTag ACCOUNTS = LogTag.from("#be_kbc_accounts");
        public static final LogTag CREDIT_CARDS = LogTag.from("#be_kbc_credit_cards");
    }

    public static final ImmutableMap<String, AccountTypes> ACCOUNT_TYPES = ImmutableMap.<String, AccountTypes>builder()
            .put("3844", AccountTypes.CHECKING)
            .put("3591", AccountTypes.SAVINGS)
            .build();

    public static final class ErrorMessage {
        public static final String INCORRECT_CARD_NUMBER = "the card number you have entered is incorrect";
        public static final String NO_TRANSACTIONS_FOUND = "no transactions in the most recent 12 months";
        public static final String NOT_A_CUSTOMER = "managing branch not found";
        public static final String INCORRECT_LOGIN_CODE = "you have entered the wrong login code";
        public static final String INCORRECT_SIGN_CODE = "your sign code is incorrect";
    }

    public enum UserMessage implements LocalizableEnum {
        INCORRECT_CARD_NUMBER(new LocalizableKey("The card number you have entered is incorrect. Please try again.")),
        NOT_A_CUSTOMER(new LocalizableKey("The provided credentials are not for KBC bank"));

        private LocalizableKey userMessage;

        UserMessage(LocalizableKey userMessage) {
            this.userMessage = userMessage;
        }

        @Override
        public LocalizableKey getKey() {
            return userMessage;
        }
    }

    public static class Transfers {
        public static final double MIN_AMOUNT = 0.01;
        public static final int MAX_MSG_LENGTH = 70;
        public static final String TRANSFER_TYPE_CODE = "0";
        public static final String SCASH_VERSION_NUMBER = "";
        public static final String TRANSFER_TO_OWN_ACCOUNT = "transferOwnAccount";
        public static final String TRANSFER_TO_OTHER_ACCOUNT = "transferOtherAccount";

        public static final String SIGN_INSTRUCTIONS = "Insert your card into the card reader and press SIGN twice."
                + "Enter the control code on your card reader and press OK. Then enter the amount of your transfer, "
                + "including the digits after the decimal sign, and press OK. Enter and your PIN and press OK, then "
                + "enter the code from the card reader in the response code field.";
        public static final String MSG_LENGTH_EXCEEDS_MAX = String.format(
                "Reference must be max %d characters.", MAX_MSG_LENGTH);
        public static final String MISSING_SOURCE_NAME = "Source name must be specified";
        public static final String MISSING_DESTINATION_NAME = "Destination name must be specified";
        public static final String DUE_DATE_TOO_FAR_IN_FUTURE = "Due date must be within a year from today";
        public static final String AMOUNT_LESS_THAN_MIN = String.format("Transfer amount can't be less than %.2f", MIN_AMOUNT);
    }
}
