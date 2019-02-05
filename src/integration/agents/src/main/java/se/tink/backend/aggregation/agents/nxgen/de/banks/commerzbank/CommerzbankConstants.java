package se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank;

import se.tink.backend.aggregation.agents.utils.log.LogTag;

public class CommerzbankConstants {

    public static final String MULTIPLE_SPACES = "  ";
    public static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";
    public static final String TIMEZONE_CET = "CET";

    public static class URLS {
        public static final String HOST = "https://app.commerzbank.de";
        public static final String LOGIN = "/app/lp/v4/applogin";
        public static final String OVERVIEW = "/app/rest/v3/financeoverview";
        public static final String TRANSACTIONS = "/app/rest/transactionoverview";
        public static final String LOGOUT = "/app/lp/v3/logout";
    }

    public static class HEADERS {
        public static final String CONTENT_TYPE = "Content-Type";
        public static final String CCB_CLIENT_VERSION = "CCB-Client-Version";
        public static final String USER_AGENT = "User-Agent";
        public static final String PRODUCT_TYPE = "productType";
        public static final String IDENTIFIER = "identifier";
        public static final String CREDIT_CARD_PRODUCT_TYPE = "creditcardProductType";
        public static final String CREDIT_CARD_IDENTIFIER = "creditcardIdentifier";
        public static final String PRODUCT_BRANCH = "productBranch";
    }

    public static class VALUES {
        public static final String JSON = "application/json";
        public static final String CCB_VALUE = "MobBkniOS+10.0.0+10.3.1";
        public static final String USER_AGENT_VALUE = "MobBkniOS-10.0.0";
        public static final String SESSION_TOKEN_VALUE = "false";
        public static final String CURRENCY_VALUE = "EUR";
        public static final String AMOUNT_TYPE = "ALL";
        public static final String LOGOUT_OK = "logoutText.ok";
    }

    public static class ACCOUNTS {
        public static final String SAVINGS_ACCOUNT = "Sparkonto";
        public static final String CREDIT_CARD = "Kreditkarten";
    }

    public static class DISPLAYCATEGORYINDEX {
        public static final int CHECKING = 1;
        public static final int SAVINGS_OR_INVESTMENT = 2;
        public static final int CREDIT = 3;
    }

    public static class ERRORS {
        public static final String PIN_ERROR = "login.pin.error.10203";
        public static final String ACCOUNT_SESSION_ACTIVE_ERROR = "login.pin.error.10205";
    }

    public static class LOGTAG {
        public static final LogTag CREDIT_CARD_FETCHING_ERROR = LogTag.from("#commerzbank_credit_card_fetching_error");
        public static final LogTag UNKNOWN_ACCOUNT_TYPE = LogTag.from("#commerzbank_unknown_account_type");
        public static final LogTag TRANSACTION_FETCHING_ERROR = LogTag.from("#commerzbank_transaction_fetching_error");
    }

}
