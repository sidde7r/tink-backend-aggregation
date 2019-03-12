package se.tink.backend.aggregation.agents.nxgen.be.banks.ing;

import se.tink.backend.aggregation.agents.utils.log.LogTag;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.libraries.i18n.LocalizableEnum;
import se.tink.libraries.i18n.LocalizableKey;

import java.util.regex.Pattern;

public class IngConstants {

    public static final String APP_VERSION = "8.11";

    public static final class Urls {
        static final String HOST = "https://mobilebanking.ing.be";

        static final URL MOBILE_HELLO = new URL(HOST + "/hb_mp/eb/ebmp/MobileHello?rootRef=/eb");
        static final URL MENU_ITEMS = new URL(HOST + "/api/homebank/sl-menu/items/en");
        public static final String BASE_SSO_REQUEST = HOST + "/hb_ms/eb/MobileSSORequest?";

    }

    public static final class Headers {
        public static final String USER_AGENT = "Mozilla/5.0 (iPhone8,1; U; CPU OS 10.3.1 like Mac OS X; en-us) "
                + "AppleWebKit/531.21.10 (KHTML, like Gecko) Version/4.0.4 Mobile/7B334b Safari/531.21.10; MyING.be/"
                + APP_VERSION;

        public static final String LOCATION = "location";
        public static final String TAM_ERROR = "tam_op=error";
        public static final String ERROR_CODE_WRONG_OTP = "error_code=3";
        public static final String ERROR_CODE_LOGIN = "error_code=2";

    }

    static final class Crypto {
        static final String RSA_MODULUS_IN_HEX = "00a7de4b19a0a738653bb4277b01d2a05b6916a616038ed23de75e8ce"
                + "615290aebf31b826b9589e1d4eec160239ad144d6056e7c475cc76564474df659830b70b0fb7815cf"
                + "f010421b76dc6cfec7fe67b990d4fa5f4aa346b167005b404e96cdad830e9d913a2823d4fc918df4b"
                + "7257f842b151a9880639d4b75bcbea47fd80e41c15a2ff370d6ed13d9919b02a9d5b5dfd2aa812c65"
                + "064b4f262812c4fee8e53c4845610acb743d4f6bcf4f4b1a4ad9fec9eac8c8c483939ffd66e19a87e"
                + "d2e9b646c5e268aad2583a7a5d0fe43149a6b2f8f54442c6abf154096c151919267b5272ee1cb123c"
                + "c055cbe81f01fb17a5c32f19e0eaba67164197a9a962eecfa5b5";
        static final String RSA_EXPONENT_IN_HEX = "010001";
    }

    public static final class Storage {
        public static final String URLS_BY_REQUEST_NAME_STORAGE = "__urlsByRequestName";
        public static final String DEVICE_ID = "deviceId";
        public static final String ING_ID = "ingId";
        public static final String OTP_KEY_HEX = "otpKey";
        public static final String VIRTUAL_CARDNUMBER = "virtualCardnumber";
        public static final String PSN = "psn";
        public static final String OTP_COUNTER = "otpCounter";
        public static final String SYSTEM_PIN = "systemPin";
        public static final String SECRET_0_IN_HEX = "secret0";
        public static final String SECRET_1_IN_HEX = "secret1";
        public static final String SESSION_KEY_IN_HEX = "sessionKey";
        public static final String SESSION_KEY_AUTH_IN_HEX = "sessionKeyAuth";
        public static final String LOGIN_RESPONSE = "loginResponse";
    }

    public static final class RequestNames {
        public static final String AUTHENTICATE = "authenticate";
        public static final String ENROL_DEVICE = "enroldevice";
        public static final String GET_APP_CREDENTIALS = "getappcredentials";
        public static final String PREPARE_ENROLL = "prepareenroll";
        public static final String CONFIRM_ENROLL = "confirmenroll";
        public static final String LOGOUT = "logout";
        public static final String LOGON = "logon";
        public static final String GET_ACCOUNTS = "getaccountbalanceandavailability";
        public static final String GET_TRANSACTIONS = "gettransactions";
        public static final String GET_PENDING_PAYMENTS = "getpendingpaymentsonaccount";
        public static final String CREDITCARD_LIST = "getcreditcardlist";
        public static final String CREDITCARD_TRANSACTIONS = "gettransactionsoncreditcard";
        public static final String GET_TRUSTED_BENEFICIARIES = "getmobiletrustedbeneficiaries";
        public static final String VALIDATE_INTERNAL_TRANSFER = "validatetransfer";
        public static final String EXECUTE_INTERNAL_TRANSFER = "executeTransfer";
        public static final String VALIDATE_TRUSTED_TRANSFER = "validatetrustedtransfer";
        public static final String EXECUTE_TRUSTED_TRANSFER = "executeTrustedTransfer";
        public static final String VALIDATE_THIRD_PARTY_TRANSFER = "validate3rdpartytransfer";
        public static final String EXECUTE_THIRD_PARTY_TRANSFER = "execute3rdpartytransfer";
    }

    public static final class ReturnCodes {
        public static final String OK = "ok";
        public static final String NOK = "nok";
    }

    public static final class Session {
        public static final String SECURITY_TYPE_KEY = "securityType";
        public static final String SECURITY_TYPE_UCR_VALUE = "UCR";
        public static final String SECURITY_TYPE_MOB_VALUE = "MOB";
        public static final String REQUEST_TYPE_KEY = "RequestType";
        public static final String REQUEST_TYPE_ENROLL_VALUE = "enroll";
        public static final String REQUEST_TYPE_LOGIN_VALUE = "login";
        public static final String CARD_NR = "cardNr";
        public static final String ENC_QUERY_DATA = "ENC_QUERY_DATA";
        public static final String LOGON_TIMESTAMP = "logonTimeStamp";
        public static final String OTP = "otp";
        public static final String SIGNING_ID = "signingId";
        public static final String OTP_VALUE = "otpValue";
        public static final String OTP_SYSTEM = "otpSystem";
        public static final String MAC_ADDRESS = "macAddress";

        public enum ValuePairs {
            USER_LANG_CODE("userLanguageCode", "EN"),
            LANG_AT_AUTH("langatauth", "/EN/"),
            LANG("lang", "/EN/"),
            APP_IDENTIFICATION("applicationIdentification", APP_VERSION),
            USER_PROFILE("userProfile", "M_iOS"),
            CHANNEL_CODE("channelCode", "4"),
            APP_NAME("AppName", "1"),
            APP_CODE("appCode", "4"),
            APP_TYPE("appType", "1"),
            DEVICE_TYPE("deviceType", "1"),
            OS_TYPE("osType", "1"),
            OS_VERSION("osVersion", "10.3.1"),
            DEVICE_IS_JAILBROKEN("deviceJailBroken", "0"),
            DEVICE_NAME("deviceName", "Tink Mobile"),
            PROFILE_NAME("profileName", "Tink"),
            IP_ADDRESS("ipAddress", "127.0.0.1"),
            BROWSER_APP_VERSION("browserAppVersion", Headers.USER_AGENT),
            LOGON_TYPE("logonType", "01"),
            METHOD("METHOD", "GETAPPLICATIONSCREDENTIALS"),
            PROTOCOL_VERSION("PROTOCOL_VERSION", "3"),
            PUBLIC_KEY_ID("PUBLIC_KEY_ID", "eps-public-key"),
            FLAG_TRUSTED_BENIFICIARIES("subscriptionFlagTrustedBeneficiaries", "1"),
            FLAG_THIRD_PARTY("subscriptionFlagThirdParty", "1"),
            FLAG_SIGN_BY_TWO("subscriptionFlagSignByTwo", "1"),
            PROFILE_RENEWAL("profileRenewal", "0"),
            APP_UNIQUE_ID("appUniqueId", "be.ING.MyING2"),
            PN_OPTIN("pnOptin", "0"),
            DSE_TYPE("dse_type", "post");

            private final String key;
            private final String value;

            ValuePairs(String key, String value) {
                this.key = key;
                this.value = value;
            }

            public String getKey() {
                return this.key;
            }

            public String getValue() {
                return this.value;
            }
        }
    }

    public static final class AccountTypes {
        public static final String CURRENT_ACCOUNT = "1";
        public static final String SAVINGS_ACCOUNT = "2";
        public static final String TRANSFER_TO_OWN_RULE = "OWN";
        public static final String TRANSFER_TO_ALL_RULE = "";
    }

    public static final class Transactions {
        public static final Pattern TRANSACTION_PREFIX_PATTERN =
                Pattern.compile("^((naar:|zu:|á:|to:|van:|von:|de:|from:)|(\\d{2}/\\d{2}\\s-\\s\\d{1,2}\\.\\d{2}\\s+[ap]m\\s-)).*",
                        Pattern.CASE_INSENSITIVE);
    }

    public static final class Fetcher {
        static final int START_PAGE = 0;
        public static final int MAX_TRANSACTIONS_IN_BATCH = 20;
        public static final String ACC = "acc";
        public static final String START_INDEX = "startIndex";
        public static final String END_INDEX = "endIndex";
    }

    public static final class Logs {
        public static final LogTag LOGIN_RESPONSE = LogTag.from("#ING_loginresponse");
        public static final LogTag CREDITCARDS = LogTag.from("#ING_creditcards");
        public static final LogTag CREDITCARD_TRANSACTIONS = LogTag.from("#ING_creditcard_transactions");
        public static final LogTag UNKNOWN_ERROR_CODE = LogTag.from("#ING_UNKNOWN_ERROR_CODE");
    }

    public static final class Transfers {
        public static final String P_ACCOUNT = "pAccount";
        public static final String P_ACCOUNT_313 = "pAccount313";
        public static final String P_NAME = "pName";
        public static final String P_ADDRESS = "pAddress";
        public static final String P_CITY = "pCity";
        public static final String B_ACCOUNT = "bAccount";
        public static final String B_NAME = "bName";
        public static final String AMOUNT = "amount";
        public static final String CURRENCY = "currency";
        public static final String MEMO_DATE = "memoDate";
        public static final String STRUCTURED_COMMUNICATION = "structuredCommunication";
        public static final String COMM_LINE = "commLine";
        public static final String BANK_NAME = "ING Belgium";

        public static final int MAX_MESSAGE_LENGTH = 35;
        public static final int MAX_MESSAGE_ROWS = 4;

        public enum ValuePairs {
            P_COUNTRY("pCountry", ""),
            B_ADDRESS("bAddress", ""),
            B_CITY("bCity", ""),
            B_COUNTRY("bCountry", "BE");

            private final String key;
            private final String value;

            ValuePairs(String key, String value) {
                this.key = key;
                this.value = value;
            }

            public String getKey() {
                return this.key;
            }

            public String getValue() {
                return this.value;
            }
        }
    }

    public static final class ErrorCodes {
        public static final String FETCHED_TRANSACTIONS_OUTSIDE_RANGE_CODE = "ews/01/l130-000";
        public static final String TRANSFER_AMOUNT_EXCEEDS_LIMIT_CODE = "e51/trsg048-000";
        public static final String TRANSFER_AMOUNT_EXCEEDS_BALANCE = "e52/trsg014-000";
        public static final String STARTING_DATE_ENTERED_IS_WRONG = "e50/01/g036-000";
        public static final String NO_ACCESS_TO_ONLINE_BANKING = "e50/01/g350-220";
        public static final String NO_LINKED_ACCOUNT = "e50/01/g353-000";
        public static final String ACCOUNT_CANCELLED = "e50/01/g322-000";
        public static final String OUT_OF_SESSION = "001";
       }

       public static final class ErrorText {
           public static final String OUT_OF_SESSION = "out of session";
       }

    public enum EndUserMessage implements LocalizableEnum {
        INCORRECT_LOGIN_CREDENTIALS(new LocalizableKey("Check if your authentication parameters are correct:\n"
                + "1. ING ID from your bank card\n2. Card ID from your bank card\n3. \"RESPONSE\" from the ING"
                + " Card Reader")),
        TRANSFER_AMOUNT_EXCEEDS_LIMIT(new LocalizableKey("The amount of this transaction exceeds your transfer"
                + " limits. Please log in to Home'Bank to check your transfer limits.")),
        TRANSFER_TO_EXTERNAL_ACCOUNTS_NOT_ALLOWED(
                new LocalizableKey("Transfers to accounts from other banks is not allowed from this account.")),
        DATE_MUST_BE_BUSINESS_DAY(
                new LocalizableKey("Execution date have to be a business day, choose another date.")),
        MISSING_DESTINATION_NAME(
                new LocalizableKey("Destination name have to be supplied for third party transfers.")),
        TRANSFER_VALIDATION_FAILED(new LocalizableKey("Transfer validation failed.")),
        TRANSFER_EXECUTION_FAILED(new LocalizableKey("Transfer execution failed."));

        private LocalizableKey userMessage;

        EndUserMessage(LocalizableKey userMessage) {
            this.userMessage = userMessage;
        }

        @Override
        public LocalizableKey getKey() {
            return userMessage;
        }
    }

    public static final class LogMessage {
        public static final String CHALLENGE_EXCHANGE_ERROR = "#ING_challenge_exchange_failed";
        public static final String UNKNOWN_RETURN_CODE = "#ING_unknown_returncode";
        public static final String UNKNOWN_LOCATION_CODE = "#ING_unknown_location";
        public static final String URL_NOT_FOUND = "#ING_url_not_found";
        public static final String TRANSACTION_FETCHING_ERROR = "#ING_transaction_fetching_failed";
        public static final String LOGIN_RESPONSE_NOT_FOUND = "Could not fetch login response.";
        public static final String TRANSFER_ACCOUNTS_NOT_FOUND = "Could not fetch users' accounts when for transfer.";
    }
}
