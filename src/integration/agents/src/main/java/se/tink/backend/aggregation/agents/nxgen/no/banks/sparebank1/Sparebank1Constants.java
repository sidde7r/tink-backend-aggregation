package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1;

import se.tink.backend.aggregation.nxgen.http.URL;

public class Sparebank1Constants {

    public static class Urls {
        public static final String BASE = "https://mobilbank-pm.sparebank1.no/";
        public static final String BASE_LOGIN = "https://login.sparebank1.no/";

        public static final URL CMS = new URL(BASE + "personal/rest/cms");
        public static final URL LOGIN_DISPATCHER =
                new URL(BASE_LOGIN + "auth/pages/loginDispatcher.xhtml");
        public static final URL SELECT_MARKET_AND_AUTH_TYPE =
                new URL(BASE_LOGIN + "auth/pages/selectMarketAndAuthType.xhtml");
        public static final URL POLL_BANKID = new URL(BASE_LOGIN + "auth/api/bim/poll");
        public static final URL LOGIN_DONE = new URL(BASE_LOGIN + "auth/pages/loginDone.xhtml");
        public static final URL CONTINUE_ACTIVATION =
                new URL(BASE + "personal/activation/continue-activation");
        public static final URL AGREEMENTS =
                new URL(BASE + "{bankName}/nettbank-privat/avtale/rest/avtale");

        public static final URL ACCOUNTS =
                new URL(BASE + "{bankName}/nettbank-privat/rest/accounts");
        public static final URL CREDITCARDS =
                new URL(BASE + "{bankName}/nettbank-privat/kort/rest/cards/credit");
        public static final URL LOANS = new URL(BASE + "{bankName}/nettbank-privat/rest/loans");
        public static final URL LOAN_DETAILS =
                new URL(BASE + "{bankName}/nettbank-privat/rest/loans/{accountId}");
        public static final URL CREDITCARD_TRANSACTIONS =
                new URL(
                        BASE
                                + "{bankName}/nettbank-privat/kort/rest/cards/credit/{accountId}/transactions");
        public static final URL PORTFOLIOS =
                new URL(BASE + "{bankName}/nettbank-privat/sparing/rest/fond/portefoeljer");
    }

    public static class Parameters {
        public static final String BANK_NAME = "bankName";
        public static final String ACCOUNT_ID = "accountId";
        public static final String CID = "cid";
        public static final String CID_VALUE = "1";
    }

    public static class QueryParams {
        public static final String APP = "app";
        public static final String APP_VALUE = "mobilbank";
        public static final String FIN_INST = "finInst";
        public static final String GOTO = "goto";
        public static final String MARKET = "market";
        public static final String MARKET_VALUE = "PRIVATE";
        public static final String CONTINUE_ACTIVATION_URL =
                "https://mobilbank-pm.sparebank1.no/personal/activation/continue-activation&cid=1";
        public static final String UNDERSCORE = "_";
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
        public static final String REFERER_FOR_FINISH_AGREEMENT_SESSION =
                "/nettbank-privat/avtale/velg-avtale?goto=https://mobilbank-pm.sparebank1.no/personal/activation/continue-activation";
        public static final String X_SB1_REST_VERSION = "X-SB1-Rest-Version";
        public static final String X_SB1_REST_VERSION_VALUE = "1.0.0";
        public static final String X_REQUESTED_WITH = "X-Requested-With";
        public static final String XML_HTTP_REQUEST = "XMLHttpRequest";
        public static final String CSRFT_TOKEN = "X-CSRFToken";
    }

    public static final class Keys {
        public static final String LOGOUT_KEY = "logout";
        public static final String KEEP_ALIVE_KEY = "keepAlive";
        public static final String CHALLENGE_KEY = "challenge";
        public static final String VALIDATE_SESSION_KEY = "validateSessionKey";
        public static final String LOGIN_KEY = "login";
        public static final String ACTIVATION_KEY = "activation";
        public static final String TRANSACTIONS_KEY = "transactions";
        public static final String REST_ROOT_KEY = "restRoot";
        public static final String MORE_TRANSACTIONS_KEY = "moreTransactions";
        public static final String PORTFOLIO_HOLDINGS_KEY = "portfolioHoldings";
        public static final String SESSION_ID = "dsessionid";
        public static final String TRANSACTIONS_LINK = "transactionsLink";
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

    public static final class ErrorMessages {
        public static final String SRP_BAD_CREDENTIALS = "error.srp.bad.credentials";
        public static final String SERVICE_UNAVAILABLE = "SERVICE_UNAVAILABLE";
    }
}
