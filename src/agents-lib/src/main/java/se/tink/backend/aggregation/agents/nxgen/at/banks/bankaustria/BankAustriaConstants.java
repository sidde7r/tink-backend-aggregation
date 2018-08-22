package se.tink.backend.aggregation.agents.nxgen.at.banks.bankaustria;

import se.tink.backend.aggregation.agents.utils.log.LogTag;
import se.tink.backend.aggregation.nxgen.http.URL;

public class BankAustriaConstants {

    public static final String NOT_OK = "ko";
    public static final String OK = "ok";
    public static final String CURRENT = "CURRENT";


    public static final class Urls {
        static final String HOST = "https://mobile.bankaustria.at";
        static final URL LOGIN = new URL(HOST + "/IBOA/login.htm");
        static final URL SETTINGS = new URL(HOST + "/IBOA/manageFavouritesAccountController.htm");
        static final URL MOVEMENTS = new URL(HOST + "/IBOA/balanceMovements.htm");
        static final URL UPDATE_PAGE = new URL(HOST + "/IBOA/otmlUpdate.htm");
        static final URL LOGOUT = new URL(HOST + "/IBOA/otml_v2.0/maps/generic/logout_popup.xml");
    }

    public static final class Application {
        static final String PLATFORM = "ios";
        static final String PLATFORM_VERSION = "iPhone_Bank Austria_41_4.7.1";
        static final String OTMLID = "1.07";
    }

    public static final class Device {
        static final String IPHONE7_RESOLUTION = "{750, 1334}";
        static final String IPHONE7_DEVICEID = "Apple_iPhone7,2_iOS_11.1.2";
        static final String IPHONE7_OTML_LAYOUT_INITIAL = "97E5F84071E93B799B749A8FFAD8881B";
        static final String IPHONE7_USERAGENT = "Mozilla/5.0 (iPhone; U; CPU OS 3_2 like Mac OS X; en-us) AppleWebKit/531.21.10 (KHTML, like Gecko) Version/4.0.4 Mobile/7B334b Safari/531.21.10";
    }

    public static final class Header {
        public static final String USER_AGENT = "User-Agent";
        public static final String X_OTML_CLUSTER = "X-OTML-CLUSTER";
        public static final String X_DEVICE = "X-DEVICE";
        public static final String X_OTML_MANIFEST = "X-OTML-MANIFEST";
        public static final String X_OTML_PLATFORM = "X-OTML-PLATFORM";
        public static final String X_OTMLID = "X-OTMLID";
        public static final String X_APPID = "X-APPID";
        static final String MANIFEST = "X-OTML-MANIFEST";
    }

    public static final class SettingsForm {
        public static final String SETTINGS_TARGET = "_target0";
        public static final String SETTINGS_TARGET_VALUE = "xxx";
    }

    public static final class LoginForm {
        public static final String PASSWORD = "password";
        public static final String OTML_SECURE_ENCLAVE_PARAMS = "otml_secure_enclave_params";
        public static final String USE_LOGIN_VIA_FINGERPRINT = "useLoginViaFingerprint";
        public static final String ACTIVATE_FINGERPRINT_MESSAGE = "activate_fingerprint_message";
        public static final String LANGUAGE = "language";
        public static final String SUPPORT_FACE_ID = "supportFaceId";
        public static final String OPTPARAM = "optparam";
        public static final String SECURITY_CHECK_AVAILABLE = "securityCheckAvailable";
        public static final String SUPPORT_FACE_ID_FALSE = "false";
        public static final String LANGUAGE_EN_US = "en_US";
        public static final String ACTIVATE_FINGERPRINT_LOGIN_VALUE = "Activate%20fingerprint%20login";
        public static final String OTML_SECURE_ENCLAVE_TOKEN = "token";
    }

    public static final class MovementsSearchForm {
        public static final String SEARCH_TYPE = "searchType";
        public static final String ACCOUNT_ID = "account.id";
        public static final String START_YEAR = "start.year";
        public static final String START_MONTH = "start.month";
        public static final String START_DAY = "start.day";
        public static final String END_YEAR = "end.year";
        public static final String END_MONTH = "end.month";
        public static final String END_DAY = "end.day";
        public static final String TYPE = "type";
        public static final String SOURCE = "source";
        public static final String AMOUNT_TO_POSITIVITY = "amountToPositivity";
        public static final String AMOUNT_FROM_POSITIVITY = "amountFromPositivity";
        public static final String OTML_BIND_SIDE_CONTENT = ".otmlBind->side_content";
        public static final String FINISH = "_finish";
        public static final String OTML_FIRST_SEARCH = "otmlFirstSearch";
        public static final String AMOUNT_FROM = "amountFrom";
        public static final String AMOUNT_TO = "amountTo";
        public static final String TEXT = "text";
        public static final String SEARCH_TYPE_VALUE_LAST_30_DAYS = "30";
        public static final String TYPE_ALL = "all";
        public static final String SOURCE_OTML = "otml";
        public static final String FINISH_VALUE = "xxx";
        public static final String OTML_FIRST_SEARCH_VALUE_FALSE = "false";
        public static final String SEARCH_TYPE_VALUE_CUSTOM = "custom";
    }

    public static final class XPathExpression {
        public static final String XPATH_RESPONSE_RESULT = "/datasources/datasource[@key='response']/element[@key='result']";
        public static final String XPATH_SETTINGS_RESPONSE_ACCOUNTS = "/datasources/datasource[@key='response']/element[@key='customizedAccountMetaModelsList']/element";
        public static final String XPATH_ACCOUNT_NUMBER = ".//element[@key='accountNumber']";
        public static final String XPATH_ACCOUNT_NICKNAME = ".//element[@key='accountNickname']";
        public static final String XPATH_ACCOUNT_KEY = ".//element[@key='accountKey']";
        public static final String XPATH_SETTINGS_ACCOUNT_TYPE = ".//element[@key='accountType']";
        public static final String XPATH_ACCOUNT_BALANCE = "/datasources/datasource[@key='response']/element[@key='balance']/element[@key='accountable']";
        public static final String XPATH_CURRENCY = ".//element[@key='currency']";
        public static final String XPATH_VALUE = ".//element[@key='value']";
        public static final String XPATH_ACCOUNT_IBAN = "/datasources/datasource[@key='response']/element[@key='account']/element[@key='iban']";
        public static final String XPATH_ACCOUNT_COMPANIES = "/datasources/datasource[@key='response']/element[@key='account']/element[@key='companies']";
        public static final String XPATH_NAME = ".//element[@key='name']";
        public static final String XPATH_ACCOUNT_TYPE = "/datasources/datasource[@key='response']/element[@key='account']/element[@key='type']";
        public static final String XPATH_ACCOUNT_DATABASE_CODE = "/datasources/datasource[@key='response']/element[@key='account']/element[@key='dataBaseCode']";
        public static final String XPATH_TRANSACTIONS_MOVEMENTS = "/datasources/datasource[@key='response']/element[@key='movements']/element";
        public static final String XPATH_TRANSACTION_CURRENCY = ".//element[@key='amount']/element[@key='currency']";
        public static final String XPATH_TRANSACTION_VALUE = ".//element[@key='amount']/element[@key='value']";
        public static final String XPATH_TRANSACTION_DESCRIPTION = ".//element[@key='bookingText']";
        public static final String XPATH_TRANSACTION_DATE = ".//element[@key='transactionDate']/element[@key='date']";
        public static final String XPATH_RESPONSE_WITH_ACCOUNT = "/datasources/datasource[@key='otml_store_session']/element[@key='account']/element";
    }

    public static class LogTags {
        public static final LogTag LOG_TAG_ACCOUNT = LogTag.from("#BankAustria_account");
        public static final LogTag LOG_TAG_CODE_ERROR = LogTag.from("#BankAustria_code_error");
    }

}
