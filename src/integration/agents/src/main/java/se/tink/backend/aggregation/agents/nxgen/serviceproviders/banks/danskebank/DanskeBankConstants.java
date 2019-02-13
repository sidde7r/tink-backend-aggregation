package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank;

import se.tink.backend.aggregation.agents.utils.log.LogTag;

import java.text.MessageFormat;

public final class DanskeBankConstants {
    public class Url {
        private static final String HOST = "https://apiebank.danskebank.com";
        private static final String BASE = HOST + "/ebanking/ext";
        private static final String E4_BASE = BASE + "/e4";
        private static final String FI_BASE = BASE + "/fi";

        public static final String DYNAMIC_JS_AUTHENTICATE = BASE + "/Functions?stage=LogonStep1&secsystem=%s&brand=%s&channel=MOB";
        public static final String DYNAMIC_JS_AUTHORIZE = BASE + "/Functions?stage=Postlogon";

        // == START Authentication ==
        public static final String BANKID_INIT_LOGON = BASE + "/swedinitlogon";
        public static final String BANKID_POLL = BASE + "/swedpoll";

        public static final String DEVICE_BIND_CHECK = BASE + "/devicebind/check";
        public static final String DEVICE_BIND_BIND = BASE + "/devicebind/bind";
        public static final String DEVICE_LIST_OTP = BASE + "/ListOTP";

        public static final String FINALIZE_AUTHENTICATION = BASE + "/logon";
        // == END Authentication ==

        // == START Accounts ==
        public static final String LIST_ACCOUNTS = E4_BASE + "/account/list";
        public static final String LIST_CUSTODY_ACCOUNTS = FI_BASE + "/initialization/get";
        // == END Accounts ==

        // == START Loans ==
        public static final String LIST_LOANS = E4_BASE + "/realestate/loan/list";
        public static final String LOAN_DETAILS = E4_BASE + "/realestate/loan/detail";
        // == END Loans ==

        // == START Credit card info ==
        public static final String LIST_CARD_INFO =  BASE + "/kk/api/cards/getlist";
        // == END Credit card info ==

        // == START Instruments ==
        public static final String LIST_SECURITIES = FI_BASE + "/marketvalue/listSecurities";
        public static final String LIST_SECURITY_DETAILS = FI_BASE + "/securityDetails/getSecurityDetails";
        // == END Instruments ==

        // == START Transactions ==
        public static final String LIST_TRANSACTIONS = E4_BASE + "/transaction/list";
        public static final String LIST_UPCOMING_TRANSACTIONS = E4_BASE + "/transaction/future";
        // == END Transactions

        // == START SessionHandler ==
        public static final String EXTEND_SESSION = BASE + "/extend";
        // == END SessionHandler ==

    }

    public static class Account {
        public static final String CREDIT_CARD_CODE = "101";
    }

    public static class Investment {
        public static final String CUSTODY_ACCOUNT = "custodyaccount";
        public static final int INSTRUMENT_TYPE_STOCK = 1;
        public static final int INSTRUMENT_TYPE_BOND = 2;
        public static final int INSTRUMENT_TYPE_FUND = 3;
        public static final int INSTRUMENT_TYPE_INVESTMENT_ASSOCIATION = 4;
        public static final int INSTRUMENT_TYPE_STRUCTURED_PRODUCT = 5;
    }

    public class SecuritySystem {
        public static final String SERVICE_CODE_SC = "SC";
        public static final String SERVICE_CODE_JS = "JS";
        public static final String CHALLENGE_INVALID = "INVALID";
    }

    public static class LogTags {
        public static final LogTag AUTHENTICATION_BANKID = LogTag.from("Danske Bank - Authentication - BankID");
        public static final LogTag AUTHENTICATION_AUTO = LogTag.from("Danske Bank - Authentication - Auto");
        public static final LogTag LOAN_ACCOUNT = LogTag.from("Loan_account");
        public static final LogTag TRANSACTIONAL_ACCOUNT = LogTag.from("Danske Bank - Transactional account");
    }

    public static class Javascript {
        private static final String DEVICE_ID = " ";
        private static final String DEVICE_NAME = "Tink";
        public static final String USER_AGENT = "Mozilla/5.0 (iPhone; CPU iPhone OS 10_1_1 like Mac OS X) AppleWebKit/602.2.14 (KHTML, like Gecko) Mobile/14B100";
        private static final String LANGUAGE = "en";
        private static final String PRODUCT_SUB = "10.1.1";
        private static final String PLATFORM = "iOS";
        private static final String SCREEN_RESOLUTION = "750x1334";
        private static final String TIMEZONE = "Europe/Stockholm";
        private static final String TIMEZONE_OFFSET = "3600.000000";
        private static final String FONT = "Academy Engraved LET,Al Nile,American Typewriter,Apple Color Emoji,Apple SD Gothic Neo,Arial,Arial Hebrew,Arial Rounded MT Bold,Avenir,Avenir Next,Avenir Next Condensed,Bangla Sangam MN,Baskerville,Bodoni 72,Bodoni 72 Oldstyle,Bodoni 72 Smallcaps,Bodoni Ornaments,Bradley Hand,Chalkboard SE,Chalkduster,Cochin,Copperplate,Courier,Courier New,Damascus,Danske Human,Danske Text v2,danskenarrow,DanskeNarrow,Devanagari Sangam MN,Didot,Euphemia UCAS,Farah,Futura,Geeza Pro,Georgia,Gill Sans,Gujarati Sangam MN,Gurmukhi MN,Heiti SC,Heiti TC,Helvetica,Helvetica Neue,Hiragino Mincho ProN,Hiragino Sans,Hoefler Text,Kailasa,Kannada Sangam MN,Khmer Sangam MN,Kohinoor Bangla,Kohinoor Devanagari,Kohinoor Telugu,Lao Sangam MN,Malayalam Sangam MN,Marker Felt,Menlo,Mishafi,Myanmar Sangam MN,Noteworthy,Optima,Oriya Sangam MN,Palatino,Papyrus,Party LET,PingFang HK,PingFang SC,PingFang TC,Savoye LET,Sinhala Sangam MN,Snell Roundhand,Symbol,Tamil Sangam MN,Telugu Sangam MN,Thonburi,Times New Roman,Trebuchet MS,Verdana,Zapf Dingbats,Zapfino";
        private static final String MODEL = "iPhone9,3";
        private static final String ERROR = "";
        private static final String DEVICE_INFO = "\"window.getDeviceInformationString = function(arg1) '{' arg1('{'\\n  \\\"deviceId\\\": \\\"{0}\\\",\\n  \\\"deviceName\\\": \\\"{1}\\\",\\n  \\\"generatedId\\\": \\\"{2}\\\",\\n  \\\"userAgent\\\": \\\"{3}\\\",\\n  \\\"language\\\": \\\"{4}\\\",\\n  \\\"country\\\": \\\"{5}\\\",\\n  \\\"productSub\\\": \\\"{6}\\\",\\n  \\\"platform\\\": \\\"{7}\\\",\\n  \\\"appCodeName\\\": \\\"{8}\\\",\\n  \\\"appVersion\\\": \\\"{9}\\\",\\n  \\\"screenResolution\\\": \\\"{10}\\\",\\n  \\\"timezone\\\": \\\"{11}\\\",\\n  \\\"timezoneOffset\\\": \\\"{12}\\\",\\n  \\\"fonts\\\": \\\"{13}\\\",\\n  \\\"model\\\": \\\"{14}\\\",\\n  \\\"error\\\": \\\"{15}\\\"\\n'}');'}' \" + ";

        public static String getDeviceInfo(String generateId, String country, String appCodeName, String appVersion) {
            return MessageFormat.format(DEVICE_INFO, DEVICE_ID, DEVICE_NAME, generateId, USER_AGENT, LANGUAGE, country,
                    PRODUCT_SUB, PLATFORM, appCodeName, appVersion, SCREEN_RESOLUTION, TIMEZONE, TIMEZONE_OFFSET, FONT,
                    MODEL, ERROR);
        }
    }

    public class Session {
        public static final String FRIENDLY_NAME = "Tink";
    }

    public class Persist {
        public static final String DEVICE_SECRET = "DeviceSecret";
        public static final String DEVICE_SERIAL_NUMBER = "DeviceSerialNumber";
    }
}
