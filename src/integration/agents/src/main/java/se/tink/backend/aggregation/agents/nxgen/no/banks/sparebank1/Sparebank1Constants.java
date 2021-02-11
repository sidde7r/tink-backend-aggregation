package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1;

import se.tink.backend.aggregation.nxgen.http.url.URL;

public class Sparebank1Constants {

    public static class Urls {
        static final String BASE = "https://mobilbank-pm.sparebank1.no/";
        static final String BASE_LOGIN = "https://login.sparebank1.no/";

        static final URL SELECT_MARKET_AND_AUTH_TYPE =
                new URL(BASE_LOGIN + "auth/pages/selectMarketAndAuthType.xhtml");
        static final URL POLL_BANKID = new URL(BASE_LOGIN + "auth/api/bim/poll");
        static final URL LOGIN_DONE = new URL(BASE_LOGIN + "auth/pages/loginDone.xhtml");
        public static final URL ACCOUNT_DETAILS =
                new URL(BASE + "{bankName}/nettbank-privat/rest/konto/{accountId}/detaljer");
        public static final URL ACCOUNTS_IDENTIFIERS =
                new URL(BASE + "{bankName}/nettbank-privat/rest/accounts");
        public static final URL ACCOUNTS =
                new URL(BASE + "{bankName}/nettbank-privat/rest/kontoer");
        public static final URL CREDITCARDS =
                new URL(BASE + "{bankName}/nettbank-privat/kort/rest/cards/v2/credit");
        public static final URL LOANS = new URL(BASE + "{bankName}/nettbank-privat/rest/loans");
        static final URL LOAN_DETAILS =
                new URL(BASE + "{bankName}/nettbank-privat/rest/loans/{accountId}");
        static final URL CREDITCARD_TRANSACTIONS =
                new URL(
                        BASE
                                + "{bankName}/nettbank-privat/kort/rest/cards/credit/{accountId}/transactions");
        public static final URL PORTFOLIOS =
                new URL(BASE + "{bankName}/nettbank-privat/sparing/rest/fond/portefoeljer");
        static final URL INIT_LOGIN =
                new URL(BASE + "personal/banking/mobilbank/login-app-dispatcher");

        static final URL AGREEMENTS = new URL(BASE + "personal/banking/authorization");
        static final URL DIGITAL_SESSION =
                new URL(BASE + "personal/banking/mobilbank/digitalbank-sessionid");
        static final URL INITIAL_REQUEST = new URL(BASE + "personal/banking/mobilbank");
        static final URL BRANCHES = new URL(BASE + "/personal/banking/bankrelations");
        static final URL TOKEN = new URL(BASE + "personal/banking/mobilbank/activation/rest/token");
        static final URL SESSION = new URL(BASE + "personal/rest/session");
        static final URL ACCOUNT_TRANSACTION =
                new URL(BASE + "sr-bank/nettbank-privat/rest/kontoer/{accountId}/kontobevegelser");
    }

    public static class Parameters {
        public static final String BANK_NAME = "bankName";
        public static final String ACCOUNT_ID = "accountId";
        public static final String CID = "cid";
        public static final String CID_VALUE = "1";
    }

    public static class QueryParams {
        public static final String APP = "app";
        public static final String MARKET = "market";
        public static final String UNDERSCORE = "_";
        public static final String FROM_DATE = "fromDate";
        public static final String TO_DATE = "toDate";
        public static final String ROW_LIMIT = "rowLimit";
        public static final String PAGINATION_LIMIT_VAL = "25000";
    }

    public static class Headers {
        public static final String USER_AGENT =
                "Mobilbank/4.5.2; mobileOS/iOS; iOSVersion/10.3.1; deviceManufacturer/Apple; deviceType/iPhone; deviceModel/iPhone-6s; requestType/rest";
        public static final String ORIGIN = "Origin";
        public static final String REFERER = "Referer";
        public static final String APPLICATION_JSON_CHARSET_UTF8 =
                "application/json; charset=utf-8";
        public static final String TEXT_HTML_APPLICATION_XHTML_XML =
                "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8";
        public static final String X_SB1_REST_VERSION = "X-SB1-Rest-Version";
        public static final String X_SB1_REST_VERSION_VALUE = "1.0.0";
        public static final String X_REQUESTED_WITH = "X-Requested-With";
        public static final String XML_HTTP_REQUEST = "XMLHttpRequest";
        public static final String CSRFT_TOKEN = "X-CSRFToken";
        public static final String V_2_JSON = "application/vnd.sparebank1.v2+json;charset=utf-8";
        public static final String V_3_JSON = "application/vnd.sparebank1.v3+json;charset=utf-8";
    }

    public static final class Keys {
        public static final String TRANSACTIONS_KEY = "transactions";
        public static final String MORE_TRANSACTIONS_KEY = "moreTransactions";
        public static final String PORTFOLIO_HOLDINGS_KEY = "portfolioHoldings";
        public static final String SESSION_ID = "dsessionid";
        public static final String TRANSACTIONS_LINK = "transactionsLink";
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
                "PniCq(]}Ft\"8]xC-java.lang.String(vYO3Z}lxqcbxK_522Q/U?_0]qT>y?Wz_.YxK7JwJ$8Ja2;99QY?i^,<job&uH3-t(ZLMaT%umLx?'\"6X_W:OA^'DK\",OWxR6u]-uUYTJPzy5z{o";
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
