package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank;

import com.google.common.base.Strings;
import java.text.MessageFormat;
import se.tink.backend.aggregation.agents.utils.log.LogTag;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.utils.deviceprofile.DeviceProfileConfiguration;
import se.tink.libraries.i18n.LocalizableKey;

public class DanskeBankConstants {

    public String getHostUrl() {
        return "https://apiebank3.danskebank.com";
    }

    public String getBaseUrl() {
        return getHostUrl() + "/ebanking/ext";
    }

    public String getE4BaseUrl() {
        return getBaseUrl() + "/e4";
    }

    public String getFIBaseUrl() {
        return getBaseUrl() + "/fi";
    }

    public String getDynamicJsAuthenticateUrl() {
        return getBaseUrl() + "/Functions?stage=LogonStep1&secsystem=%s&brand=%s&channel=MOB";
    }

    public String getDynamicJsAuthorizeUrl() {
        return getBaseUrl() + "/Functions?stage=Postlogon";
    }

    // == START Authentication ==

    public String getBankidInitLogonUrl() {
        return getBaseUrl() + "/swedinitlogon";
    }

    public String getDeviceBindCheckUrl() {
        return getBaseUrl() + "/devicebind/check";
    }

    public String getDeviceBindBindUrl(String secSystemCode) {
        if (Strings.isNullOrEmpty(secSystemCode)) {
            return getBaseUrl() + "/devicebind/bind";
        }
        return getBaseUrl() + String.format("/devicebind/bind?secsystem=%s", secSystemCode);
    }

    public String getDeviceListOtpUrl() {
        return getBaseUrl() + "/ListOTP";
    }

    public String getDeviceInitOtpUrl() {
        return getBaseUrl() + "/InitOTP";
    }

    public String getFinalizeAuthenticationUrl() {
        return getBaseUrl() + "/logon";
    }

    public final URL DANSKEID_INIT = new URL(getBaseUrl() + "/danskeid/init");

    public final URL DANSKEID_STATUS = new URL(getBaseUrl() + "/danskeid/status");

    // == END Authentication ==

    // == START BankID polling ==

    public String getBankidPollUrl() {
        return getBaseUrl() + "/swedpoll";
    }

    // == END Polling ==

    // == START Accounts ==

    public String getListAccountsUrl() {
        return getE4BaseUrl() + "/account/list";
    }

    public String getListCustodyAccountsUrl() {
        return getFIBaseUrl() + "/initialization/get";
    }

    // == END Accounts ==

    // == START Loans ==

    public String getListLoansUrl() {
        return getE4BaseUrl() + "/realestate/loan/list";
    }

    public String getLoanDetailsUrl() {
        return getE4BaseUrl() + "/realestate/loan/detail";
    }

    // == END Loans ==

    // == START Credit card info ==

    public String getListCardInfoUrl() {
        return getBaseUrl() + "/kk/api/cards/getlist";
    }

    // == END Credit card info ==

    // == START Instruments ==

    public String getListSecuritiesUrl() {
        return getFIBaseUrl() + "/marketvalue/listSecurities";
    }

    public String getListSecurityDetailsUrl() {
        return getFIBaseUrl() + "/securityDetails/getSecurityDetails";
    }

    // == END Instruments ==

    // == START Transactions ==

    public String getListTransactionsUrl() {
        return getE4BaseUrl() + "/transaction/list";
    }

    public String getListUpcomingTransactionsUrl() {
        return getE4BaseUrl() + "/transaction/future";
    }

    // == END Transactions

    // == START SessionHandler ==

    public String getExtendSessionUrl() {
        return getBaseUrl() + "/extend";
    }

    // == END SessionHandler ==

    // == START IdentityData ==

    public String getHouseholdFiUrl() {
        return getE4BaseUrl() + "/myProfile/fetchHouseholdFI";
    }

    // == END IdentityData ==

    // == START Transfers ==

    public String getListPayeesUrl() {
        return getE4BaseUrl() + "/payee/list";
    }

    public String getCreditorNameUrl() {
        return getE4BaseUrl() + "/transfers/getCreditorName";
    }

    public String getCreditorBankNameUrl() {
        return getE4BaseUrl() + "/transfers/getBankName";
    }

    public String getValidatePaymentRequestUrl() {
        return getE4BaseUrl() + "/transfers/validateBookDate";
    }

    public String getRegisterPaymentUrl() {
        return getBaseUrl() + "/ValidateSignature";
    }

    public String getAcceptSignatureUrl(String signatureType) {
        return getBaseUrl() + "/AcceptSignature?signatureType=" + signatureType;
    }

    public String getValidateGiroRequestUrl() {
        return getE4BaseUrl() + "/transfers/validateGiroSe";
    }

    public String getValidateOcrRequestUrl() {
        return getE4BaseUrl() + "/transfers/validateOcrSe";
    }

    // == END Transfers ==

    public static class Device {
        public static final String DEVICE_TYPE_CODE_APP = "CODEAPP";
        public static final String DEVICE_TYPE_OTP_CARD = "OTPCARD";
        public static final String DEVICE_TYPE_SEC_CARD = "SECCARD";
        public static final String DEVICE_TYPE_GEMALTO = "GEMALTO";
        public static final String DEVICE_TYPE_DANSKEID = "DANSKEID";
        public static final String USER_ID_TYPE = "PRIV";
        public static final String SUPPRESS_PUSH = "N";
        public static final LocalizableKey LANGUAGE_CODE = new LocalizableKey("EN");
        // This is the text shown in the code app.
        public static final LocalizableKey REGISTER_TRANSACTION_TEXT =
                new LocalizableKey("Register with Danske Mobile Banking");
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
        // like Gecko) Mobile/14B100";
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
    }
}
