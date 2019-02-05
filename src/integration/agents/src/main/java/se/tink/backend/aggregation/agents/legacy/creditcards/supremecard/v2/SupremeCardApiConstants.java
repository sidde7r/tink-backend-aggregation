package se.tink.backend.aggregation.agents.creditcards.supremecard.v2;

public class SupremeCardApiConstants {
    // Unspecified
    public static final String USER_AGENT = "Mozilla/5.0 (iPhone; CPU iPhone OS 10_3_3 like Mac OS X)";
    public static final String BANKID_QUERY_PARAMETER = "newmethod=sbid-mobil";
    public static final String ORDER_BANKID_PATH = "/order";
    public static final String CARD_VALID = "card_valid";
    public static final String TRANSACTIONS_REQUEST_YEAR_KEY = "year";
    public static final String TRANSACTIONS_REQUEST_MONTH_KEY = "month";

    // Url specific
    public static final String BASE_URL = "https://www.supremecard.se/";
    static final String MY_PAGE_URL = "min-sida/";
    static final String COLLECT_AUTH_CHECKPOINT_URL = BASE_URL + "elogin-handler?device=mobile";
    static final String ACCOUNT_INFO_URL = BASE_URL + "wp-content/plugins/rb.bank.connector/ajax/accountInfo.php";
    static final String TRANSCATIONS_URL = BASE_URL + "wp-content/plugins/rb.bank.connector/ajax/transactions.php";

    // BankId response
    static final String BANKID_STATUS_OUTSTANDING_TRANSACTION = "outstanding_transaction";
    static final String BANKID_STATUS_COMPLETE = "complete";
    static final String BANKID_STATUS_USER_SIGN = "user_sign";
    static final String BANKID_STATUS_NO_CLIENT = "no_client";
    static final String BANKID_STATUS_ALREADY_IN_PROGRESS = "already_in_progress";

    // Signicat
    static final String SIGNICAT_TICKET_URL_QUERY_KEY = "signicat.transaction_ticket";
    static final String SIGNICAT_SERVER_URL_QUERY_KEY = "signicat.server";
    static final String SIGNICAT_TICKET_KEY = "signicat.ticket";
    static final String SIGNICAT_SERVER_KEY = "signicat.server";
    static final String SIGNICAT_SERVICE_URL_KEY = "signicat.serviceUrl";

    // Complete bankId
    public static final String TARGET_PARAMETER_KEY = "TARGET";
    public static final String SAML_RESPONSE_PARAMETER_KEY = "SAMLResponse";

    // Header
    static final String REQUESTED_WITH_HEADER_KEY = "X-Requested-With";
    static final String REQUESTED_WITH_HEADER_VALUE = "XMLHttpRequest";
}
