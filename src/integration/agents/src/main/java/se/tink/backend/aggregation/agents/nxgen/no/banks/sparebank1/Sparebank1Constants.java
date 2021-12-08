package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1;

import se.tink.backend.aggregation.nxgen.http.url.URL;

public class Sparebank1Constants {

    public static class Urls {
        static final String BASE = "https://mobilbank-pm.sparebank1.no/";
        static final String BASE_LOGIN = "https://login.sparebank1.no/";

        static final URL INIT_LINKS = new URL(BASE + "personal/banking/mobilbank/support/links");
        static final URL INIT_LOGIN_APP_DISPATCHER =
                new URL(BASE + "personal/banking/mobilbank/support/login-app-dispatcher");
        static final URL INIT_AUTHENTICATION = new URL(BASE_LOGIN + "api/app/mobile/init");
        static final URL SELECT_MARKET_AND_AUTH_TYPE =
                new URL(BASE_LOGIN + "auth/pages/selectMarketAndAuthType.xhtml");
        static final URL POLL_BANKID = new URL(BASE_LOGIN + "api/app/mobile/poll");
        static final URL ACCOUNT_DETAILS =
                new URL(BASE + "{bankName}/nettbank-privat/rest/konto/{accountId}/detaljer");
        static final URL ACCOUNTS_IDENTIFIERS =
                new URL(BASE + "{bankName}/nettbank-privat/rest/accounts");
        public static final URL ACCOUNTS =
                new URL(BASE + "{bankName}/nettbank-privat/rest/kontoer");
        public static final URL CREDITCARDS =
                new URL(BASE + "{bankName}/nettbank-privat/kort/rest/cards/v2/credit");
        public static final URL LOANS =
                new URL(BASE + "{bankName}/nettbank-privat/finansiering/rest/v2/loans");
        static final URL LOAN_DETAILS =
                new URL(BASE + "{bankName}/nettbank-privat/finansiering/rest/v2/loans/{accountId}");
        static final URL CREDITCARD_TRANSACTIONS =
                new URL(
                        BASE
                                + "{bankName}/nettbank-privat/kort/rest/cards/credit/{accountId}/transactions");
        static final URL PORTFOLIOS =
                new URL(BASE + "{bankName}/nettbank-privat/sparing/rest/fond/portefoeljer");

        static final URL AGREEMENTS = new URL(BASE + "personal/banking/authorization");
        static final URL DIGITAL_SESSION =
                new URL(BASE + "personal/banking/mobilbank/support/digitalbank-sessionid");
        static final URL INITIAL_REQUEST = new URL(BASE + "personal/banking/mobilbank");
        static final URL BRANCHES = new URL(BASE + "/personal/banking/bankrelations");
        static final URL TOKEN = new URL(BASE + "personal/banking/mobilbank/activation/rest/token");
        static final URL SESSION = new URL(BASE + "personal/banking/mobilbank/session");
        static final URL ACCOUNT_TRANSACTION =
                new URL(
                        BASE
                                + "{bankName}/nettbank-privat/rest/kontoer/{accountId}/kontobevegelser");
    }

    public static class Parameters {
        static final String BANK_NAME = "bankName";
        static final String ACCOUNT_ID = "accountId";
        static final String CID = "cid";
        static final String CID_VALUE = "1";
        static final String MOBILE_NUMBER = "mobileNumber";
        static final String DATE_OF_BIRTH = "dateOfBirth";
    }

    public static class QueryParams {
        static final String UNDERSCORE = "_";
        static final String FROM_DATE = "fromDate";
        static final String TO_DATE = "toDate";
        static final String ROW_LIMIT = "rowLimit";
        static final String PAGINATION_LIMIT_VAL = "25000";
        static final String HOLDINGS = "holdings";
        static final String PERIODIC_REPORTS = "periodicReports";
        static final String ASK_PORTFOLIOS = "askPortfolios";
        static final String BANK = "bank";
        static final String LOGIN_METHOD = "login-method";
        static final String LOGIN_METHOD_VALUE = "bim";
    }

    public static class Headers {
        static final String USER_AGENT =
                "Mobilbank/4.5.2; mobileOS/iOS; iOSVersion/10.3.1; deviceManufacturer/Apple; deviceType/iPhone; deviceModel/iPhone-6s; requestType/rest";
        static final String ORIGIN = "Origin";
        public static final String REFERER = "Referer";
        static final String APPLICATION_JSON_CHARSET_UTF8 = "application/json; charset=utf-8";
        static final String TEXT_HTML_APPLICATION_XHTML_XML =
                "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8";
        static final String X_SB1_REST_VERSION = "X-SB1-Rest-Version";
        static final String X_SB1_REST_VERSION_VALUE = "1.0.0";
        static final String X_REQUESTED_WITH = "X-Requested-With";
        static final String XML_HTTP_REQUEST = "XMLHttpRequest";
        static final String CSRFT_TOKEN = "X-CSRFToken";
        static final String V_2_JSON = "application/vnd.sparebank1.v2+json;charset=utf-8";
        static final String V_3_JSON = "application/vnd.sparebank1.v3+json;charset=utf-8";
    }

    public static final class Keys {
        public static final String MORE_TRANSACTIONS_KEY = "moreTransactions";
        public static final String SESSION_ID = "dsessionid";
        public static final String DOB = "dob";
        public static final String NATIONAL_ID = "nationalId";
    }

    public static final class DeviceValues {
        public static final String DESCRIPTION = "Mobilbank: Tink";
        public static final String MANUFACTURER = "Apple";
        public static final String MODEL = "iPhone7,2";
        public static final String STRONG = "strong";
    }

    public static final class BankIdStatuses {
        public static final String WAITING = "waiting";
        public static final String COMPLETE = "complete";
    }

    public static final class AccountTypes {
        public static final String CURRENT_ACCOUNT = "currentaccount";
        public static final String SAVINGS_ACCOUNT = "savingsaccount";
        public static final String DISPOSABLE_ACCOUNT = "disposableaccount";
    }

    public static final class Tags {
        public static final String BANKID_POLL_UNKNOWN_STATUS = "no_sparebank1_bankid_polling";
        public static final String UNKNOWN_ACCOUNT_TYPE = "#no_sparebank1_unknown_account_type";
    }

    public static class BankIdErrorCodes {
        public static final String C161 = "bid-c161";
        public static final String C167 = "bid-c167";
    }

    public static final class Encryption {
        public static final String KEY =
                "#*-.>i9-(3]8h[d5java.lang.String(zZ#[cR(c&yu~A6ahE`'%mf_|iuZ;o*Xs,T6|H!NB`9/iAu[$:`W8e`_>/eu/*6Oy0j^Dm=spQ!a}+oOvT#D*Rm2<:|_Ei_,S-H5rLvA{%5U\"S#2";
        public static final String KEY_ID = "2021-48";
    }

    public static final class Claims {
        public static final String DEVICE_ID = "deviceId";
        public static final String DEVICE_DESCRIPTION = "deviceDescription";
        public static final String BASE_64_ENCODED_PUBLIC_KEY = "base64EncodedPublicKey";
        public static final String EXP = "exp";
        public static final String TYPE = "type";
        public static final String DEVICE_INFO = "deviceInfo";
        public static final String PIN_SRP_DATA = "pinSrpData";
    }

    public static final class FormParams {
        public static final String BANKID_MOBILE_NUMBER = "bankid-mobile-number";
        public static final String BANKID_MOBILE_BIRTHDATE = "bankid-mobile-birthdate";
        public static final String NESTE_MOBIL = "nesteMobil";
        public static final String NESTE = "Neste";
        public static final String JAVAX_FACES_VIEW_STATE = "javax.faces.ViewState";
    }
}
