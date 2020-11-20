package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank;

import com.google.common.base.Strings;
import java.text.MessageFormat;
import se.tink.backend.aggregation.agents.utils.log.LogTag;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.utils.deviceprofile.DeviceProfileConfiguration;
import se.tink.libraries.i18n.LocalizableKey;

public class DanskeBankConstants {

    public static class Urls {
        static final String HOST_URL = "https://apiebank3.danskebank.com";

        public static final String BASE_URL = HOST_URL + "/ebanking/ext";

        static final String E4_BASE_URL = BASE_URL + "/e4";

        static final String FI_BASE_URL = BASE_URL + "/fi";

        static final String DYNAMIC_JS_AUTHENTICATE_URL =
                BASE_URL + "/Functions?stage=LogonStep1&secsystem=%s&brand=%s&channel=MOB";

        static final String DYNAMIC_JS_AUTHORIZE_URL = BASE_URL + "/Functions?stage=Postlogon";

        // == START Authentication ==

        public static final String BANK_ID_INIT_LOGON_URL = BASE_URL + "/swedinitlogon";

        static final String DEVICE_BIND_CHECK_URL = BASE_URL + "/devicebind/check";

        static String getDeviceBindBindUrl(String secSystemCode) {
            if (Strings.isNullOrEmpty(secSystemCode)) {
                return BASE_URL + "/devicebind/bind";
            }
            return BASE_URL + String.format("/devicebind/bind?secsystem=%s", secSystemCode);
        }

        static final String DEVICE_LIST_OTP_URL = BASE_URL + "/ListOTP";

        static final String DEVICE_INIT_OTP_URL = BASE_URL + "/InitOTP";

        static final String FINALIZE_AUTHENTICATION_URL = BASE_URL + "/logon";

        static final URL DANSKEID_INIT = new URL(BASE_URL + "/danskeid/init");

        static final URL DANSKEID_STATUS = new URL(BASE_URL + "/danskeid/status");

        // == END Authentication ==

        // == START BankID polling ==

        public static final String BANKID_POLL_URL = BASE_URL + "/swedpoll";

        // == END Polling ==

        // == START Accounts ==

        static final String LIST_ACCOUNTS_URL = E4_BASE_URL + "/account/list";

        static final String LIST_CUSTODY_ACCOUNTS_URL = FI_BASE_URL + "/initialization/get";

        // == END Accounts ==

        // == START Loans ==

        static final String LIST_LOANS_URL = E4_BASE_URL + "/realestate/loan/list";

        static final String LOAN_DETAILS_URL = E4_BASE_URL + "/realestate/loan/detail";

        // == END Loans ==

        // == START Credit card info ==

        public static final String CARDS_LIST_URL = BASE_URL + "/kk/api/cards/getlist";
        public static final String CARD_DETAILS_URL = BASE_URL + "/kk/api/cards/getcarddetails";

        // == END Credit card info ==

        // == START Instruments ==

        static final String LIST_SECURITIES_URL = FI_BASE_URL + "/marketvalue/listSecurities";

        static final String LIST_SECURITY_DETAILS_URL =
                FI_BASE_URL + "/securityDetails/getSecurityDetails";

        // == END Instruments ==

        // == START Transactions ==

        static final String LIST_TRANSACTIONS_URL = E4_BASE_URL + "/transaction/list";

        static final String LIST_UPCOMING_TRANSACTIONS_URL = E4_BASE_URL + "/transaction/future";

        // == END Transactions

        // == START SessionHandler ==

        public static final String EXTEND_SESSION_URL = BASE_URL + "/extend";

        // == END SessionHandler ==

        // == START IdentityData ==

        public static final String HOUSEHOLD_FI_URL = E4_BASE_URL + "/myProfile/fetchHouseholdFI";

        // == END IdentityData ==

        // == START Transfers ==

        public static final String LIST_PAYEES_URL = E4_BASE_URL + "/payee/list";

        public static final String CREDITOR_NAME_URL = E4_BASE_URL + "/transfers/getCreditorName";

        public static final String CREDITOR_BANK_NAME_URL = E4_BASE_URL + "/transfers/getBankName";

        public static final String VALIDATE_PAYMENT_REQUEST_URL =
                E4_BASE_URL + "/transfers/validateBookDate";

        public static final String REGISTER_PAYMENT_URL = BASE_URL + "/ValidateSignature";

        public static String getAcceptSignatureUrl(String signatureType) {
            return BASE_URL + "/AcceptSignature?signatureType=" + signatureType;
        }

        public static final String VALIDATE_GIRO_REQUEST_URL =
                E4_BASE_URL + "/transfers/validateGiroSe";

        public static final String VALIDATE_OCR_REQUEST_URL =
                E4_BASE_URL + "/transfers/validateOcrSe";

        // == END Transfers ==
    }

    public static class Device {
        public static final String DEVICE_TYPE_CODE_APP = "CODEAPP";
        public static final String DEVICE_TYPE_OTP_CARD = "OTPCARD";
        public static final String DEVICE_TYPE_SEC_CARD = "SECCARD";
        public static final String DEVICE_TYPE_GEMALTO = "GEMALTO";
        public static final String DEVICE_TYPE_DANSKEID = "DANSKEID";
        public static final String USER_ID_TYPE = "PRIV";
        public static final String SUPPRESS_PUSH = "N";
        // This is the text shown in the code app.
        public static final LocalizableKey REGISTER_TRANSACTION_TEXT =
                new LocalizableKey("Register with Danske Mobile Banking");
    }

    public static class Account {
        static final String CREDIT_CARD_CODE = "101";
        static final String ALL_INVESTMENTS_GROUP = "Group";
    }

    public static class Investment {
        public static final String CUSTODY_ACCOUNT = "custodyaccount";
        public static final int INSTRUMENT_TYPE_STOCK = 1;
        public static final int INSTRUMENT_TYPE_BOND = 2;
        public static final int INSTRUMENT_TYPE_FUND = 3;
        public static final int INSTRUMENT_TYPE_INVESTMENT_ASSOCIATION = 4;
        public static final int INSTRUMENT_TYPE_STRUCTURED_PRODUCT = 5;
    }

    public static class SecuritySystem {
        public static final String SERVICE_CODE_SC = "SC";
        public static final String SERVICE_CODE_NS = "NS";
        public static final String SERVICE_CODE_JS = "JS";
        public static final String SERVICE_CODE_BD = "BD";
        public static final String CHALLENGE_INVALID = "INVALID";
    }

    public static class LogTags {
        public static final LogTag AUTHENTICATION_AUTO =
                LogTag.from("Danske Bank - Authentication - Auto");
        public static final LogTag LOAN_ACCOUNT = LogTag.from("Loan_account");
    }

    public static class Javascript {
        private static final String DEVICE_ID = " ";
        private static final String DEVICE_NAME = "Tink";
        public static final String USER_AGENT =
                DeviceProfileConfiguration.IOS_STABLE.getUserAgentEntity().getMozillaVersion()
                        + " "
                        + DeviceProfileConfiguration.IOS_STABLE
                                .getUserAgentEntity()
                                .getSystemAndBrowserInfo()
                        + " "
                        + DeviceProfileConfiguration.IOS_STABLE.getUserAgentEntity().getPlatform()
                        + " "
                        + DeviceProfileConfiguration.IOS_STABLE
                                .getUserAgentEntity()
                                .getPlatformDetails()
                        + " "
                        + DeviceProfileConfiguration.IOS_STABLE
                                .getUserAgentEntity()
                                .getExtensions();
        // "Mozilla/5.0 (iPhone; CPU iPhone OS 10_1_1 like Mac OS X) AppleWebKit/602.2.14 (KHTML,
        // like Gecko) Mobile/14B100"
        private static final String LANGUAGE = "en";
        private static final String PRODUCT_SUB = "10.1.1";
        private static final String PLATFORM = DeviceProfileConfiguration.IOS_STABLE.getOs();
        private static final String SCREEN_RESOLUTION =
                DeviceProfileConfiguration.IOS_STABLE.getScreenWidth()
                        + "x"
                        + DeviceProfileConfiguration.IOS_STABLE.getScreenHeight();
        private static final String TIMEZONE = "Europe/Stockholm";
        private static final String TIMEZONE_OFFSET = "3600.000000";
        private static final String FONT =
                "Academy Engraved LET,Al Nile,American Typewriter,Apple Color Emoji,Apple SD Gothic Neo,Arial,Arial Hebrew,Arial Rounded MT Bold,Avenir,Avenir Next,Avenir Next Condensed,Bangla Sangam MN,Baskerville,Bodoni 72,Bodoni 72 Oldstyle,Bodoni 72 Smallcaps,Bodoni Ornaments,Bradley Hand,Chalkboard SE,Chalkduster,Cochin,Copperplate,Courier,Courier New,Damascus,Danske Human,Danske Text v2,danskenarrow,DanskeNarrow,Devanagari Sangam MN,Didot,Euphemia UCAS,Farah,Futura,Geeza Pro,Georgia,Gill Sans,Gujarati Sangam MN,Gurmukhi MN,Heiti SC,Heiti TC,Helvetica,Helvetica Neue,Hiragino Mincho ProN,Hiragino Sans,Hoefler Text,Kailasa,Kannada Sangam MN,Khmer Sangam MN,Kohinoor Bangla,Kohinoor Devanagari,Kohinoor Telugu,Lao Sangam MN,Malayalam Sangam MN,Marker Felt,Menlo,Mishafi,Myanmar Sangam MN,Noteworthy,Optima,Oriya Sangam MN,Palatino,Papyrus,Party LET,PingFang HK,PingFang SC,PingFang TC,Savoye LET,Sinhala Sangam MN,Snell Roundhand,Symbol,Tamil Sangam MN,Telugu Sangam MN,Thonburi,Times New Roman,Trebuchet MS,Verdana,Zapf Dingbats,Zapfino";
        private static final String MODEL = "iPhone9,3";
        private static final String ERROR = "";
        private static final String DEVICE_INFO =
                "\"window.getDeviceInformationString = function(arg1) '{' arg1('{'\\n  \\\"deviceId\\\": \\\"{0}\\\",\\n  \\\"deviceName\\\": \\\"{1}\\\",\\n  \\\"generatedId\\\": \\\"{2}\\\",\\n  \\\"userAgent\\\": \\\"{3}\\\",\\n  \\\"language\\\": \\\"{4}\\\",\\n  \\\"country\\\": \\\"{5}\\\",\\n  \\\"productSub\\\": \\\"{6}\\\",\\n  \\\"platform\\\": \\\"{7}\\\",\\n  \\\"appCodeName\\\": \\\"{8}\\\",\\n  \\\"appVersion\\\": \\\"{9}\\\",\\n  \\\"screenResolution\\\": \\\"{10}\\\",\\n  \\\"timezone\\\": \\\"{11}\\\",\\n  \\\"timezoneOffset\\\": \\\"{12}\\\",\\n  \\\"fonts\\\": \\\"{13}\\\",\\n  \\\"model\\\": \\\"{14}\\\",\\n  \\\"error\\\": \\\"{15}\\\"\\n'}');'}' \" + ";

        public static String getDeviceInfo(
                String generateId, String country, String appCodeName, String appVersion) {
            return MessageFormat.format(
                    DEVICE_INFO,
                    DEVICE_ID,
                    DEVICE_NAME,
                    generateId,
                    USER_AGENT,
                    LANGUAGE,
                    country,
                    PRODUCT_SUB,
                    PLATFORM,
                    appCodeName,
                    appVersion,
                    SCREEN_RESOLUTION,
                    TIMEZONE,
                    TIMEZONE_OFFSET,
                    FONT,
                    MODEL,
                    ERROR);
        }
    }

    public static class Session {
        public static final String FRIENDLY_NAME = "Tink";
    }

    public static class Persist {
        public static final String DEVICE_SECRET = "DeviceSecret";
        public static final String DEVICE_SERIAL_NUMBER = "DeviceSerialNumber";
    }

    public static class PollCodeTimeoutFilter {
        public static final int MAX_POLLS_COUNTER = 50;
    }

    public static class DanskeIdStatusCodes {
        public static final String COMPLETED = "complete";
        public static final String PENDING = "pending";
        public static final String EXPIRED = "expired_to_be_changed";
    }

    public static class DanskeIdFormValues {
        public static final String EXTERNALUSERIDTYPE = "ESAFEID";
        public static final String LASTCHECK = "false";
        public static final String EXTERNALREF = "abc";
        public static final String EXTERNALTEXT = "";
        public static final String MESSAGETEMPLATEID = "MB3_Binding";
        public static final String OTPAPPTYPE = "OC";
        public static final String OTPREQUESTTYPE = "MB3_B";
        public static final String PRODUCT = "P";
    }

    public static class DanskeRequestHeaders {
        public static final String REFERRER = "Referer";
        public static final String AUTHORIZATION = "Authorization";
        public static final String PERSISTENT_AUTH = "Persistent-Auth";
    }

    public static class HttpClientParams {
        public static final int CLIENT_TIMEOUT = 60 * 1000;
    }

    public static class Transfer {
        public static final int MAX_POLL_ATTEMPTS = 90;
    }

    public static class Market {
        public static final String SE_MARKET = "SE";
        public static final String DK_MARKET = "DK";
    }
}
