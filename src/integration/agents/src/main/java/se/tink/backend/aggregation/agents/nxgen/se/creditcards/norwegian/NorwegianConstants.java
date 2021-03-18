package se.tink.backend.aggregation.agents.nxgen.se.creditcards.norwegian;

import se.tink.backend.aggregation.nxgen.http.url.URL;

public final class NorwegianConstants {

    public static final String URL_ENCODING = "UTF-8";
    public static final String CURRENCY = "SEK";
    public static final String CARD_ALIAS = "Norwegiankortet";
    public static final String IDENTIFIER = "NORWEGIAN_CARD";
    public static final String SAVINGS_IDENTIFIER = "NORWEGIAN_SAVINGS_ACCOUNT";
    public static final String SAVINGS_ALIAS = "Norwegian Sparkonto";
    public static final String DATE_FORMAT = "yyyy-MM-dd";

    private NorwegianConstants() {
        throw new AssertionError();
    }

    public static class ErrorMessages {
        public static final String ALREADY_IN_PROGRESS = "ALREADY_IN_PROGRESS";
        public static final String INVALID_SSN = "The provided ID number was invalid";
        public static final String NOT_CUSTOMER = "hittade inte kunden";
        public static final String USER_CANCEL = "USER_CANCEL";
    }

    public static class Urls {
        public static final String BASE_URL = "https://www.banknorwegian.se/";
        public static final String IDENTITY_BASE_URL = "https://identity.banknorwegian.se/";
        public static final String CREDIT_CARD_URL = BASE_URL + "MinSida/Creditcard/";
        public static final String CREDIT_CARD_OVERVIEW_URL =
                BASE_URL + "api/mypage/creditcard/overview";
        public static final String IDENTITY_URL = BASE_URL + "MinSida/Settings/ContactInfo";
        public static final String SAVINGS_ACCOUNTS_URL = BASE_URL + "MinSida/SavingsAccount/";
        public static final String CARD_TRANSACTION_URL = CREDIT_CARD_URL + "Transactions";
        public static final String SAVINGS_TRANSACTION_URL = SAVINGS_ACCOUNTS_URL + "Transactions";
        public static final String LOGIN_STATUS_URL = BASE_URL + "login/status";
        public static final URL TRANSACTIONS_PAGINATION_URL =
                new URL(BASE_URL + "MyPage2/Transaction/GetTransactionsFromTo");

        public static final String INIT_URL = "https://www.banknorwegian.se/Login";
        public static final String LOGIN_URL =
                "https://identity.banknorwegian.se/MyPage/MobiltBankId?returnUrl=";
        public static final String TARGET_URL =
                "https://identity.banknorwegian.se/MyPage/SignicatCallback?ipid=22&returnUrl=";
        public static final String ORDER = "order";
    }

    public static class BankIdProgressStatus {
        public static final String COMPLETE = "COMPLETE";
        public static final String OUTSTANDING_TRANSACTION = "OUTSTANDING_TRANSACTION";
        public static final String USER_SIGN = "USER_SIGN";
        public static final String NO_CLIENT = "NO_CLIENT";
    }

    public static class ElementNames {
        public static final String SAML_FORM = "responseForm";
        public static final String FORM = "form";
    }

    public static class ElementAttributes {
        public static final String ACTION = "action";
    }

    public static class QueryKeys {
        public static final String ACCOUNT_NUMBER = "accountNo";
        public static final String DATE_FROM = "dateFrom";
        public static final String DATE_TO = "dateTo";
        public static final String GET_LAST_DAYS = "getLastDays";
        public static final String FROM_LAST_EOC = "fromLastEOC";
        public static final String CORE_DOWN = "coreDown";
        public static final String RETURN_URL = "returnUrl";
    }

    public static class QueryValues {
        public static final String GET_LAST_DAYS_TRUE = "true";
        public static final String GET_LAST_DAYS_FALSE = "false";
        public static final String FROM_LAST_EOC = "false";
        public static final String CORE_DOWN = "false";
    }

    public static class HeaderValues {
        public static final String LANGUAGE = "en-US,en;q=0.9,sv;q=0.8";
        public static final String ACCEPT =
                "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9";
    }
}
