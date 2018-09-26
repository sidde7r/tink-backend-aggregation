package se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS;

import com.google.common.collect.ImmutableList;
import java.text.SimpleDateFormat;
import se.tink.backend.aggregation.agents.utils.log.LogTag;

public class ICSConstants {

    public static final class URL {
        public static final String AUTH_BASE = "https://auth.vvwpgvhh.icscards.nl";
        public static final String BASE = "https://api.vvwpgvhh.icscards.nl";
        public static final String ACCOUNT_SETUP = "/1/api/open-banking/v1.0/account-requests";
        public static final String ACCOUNT = "/1/api/open-banking/v1.1/accounts";
        public static final String BALANCES = "/1/api/open-banking/v1.1/accounts/%s/balances";
        public static final String TRANSACTIONS = "/1/api/open-banking/v1.1/accounts/%s/transactions";
        public static final String OAUTH_AUTHORIZE = "/openbanking-nOAuth/oauth/authorize";
        public static final String OAUTH_TOKEN = "/openbanking-oauth-tokenv1/token";
    }

    public static final class Query {
        public static final String ACCOUNT_REQUEST_ID = "AccountRequestId";
        public static final String CLIENT_ID = "client_id";
        public static final String CLIENT_SECRET = "client_secret";
        public static final String SCOPE = "scope";
        public static final String REDIRECT_URI = "redirect_uri";
        public static final String STATE = "state";
        public static final String GRANT_TYPE = "grant_type";
        public static final String RESPONSE_TYPE = "response_type";
        public static final String AUTH_CODE = "code";
        public static final String REFRESH_TOKEN = "refresh_token";


        public static final String SCOPE_ACCOUNTS = "accounts";
        public static final String GRANT_TYPE_CLIENT_CREDENTIALS = "client_credentials";
        public static final String GRANT_TYPE_AUTH_CODE = "authorization_code";
        public static final String GRANT_TYPE_REFRESH_TOKEN = "refresh_token";
        public static final String RESPONSE_TYPE_CODE = "code";
    }

    public static final class Headers {
        public static final String CLIENT_ID = "client_id";
        public static final String CLIENT_SECRET = "client_secret";

        // The unique id of the ASPSP to which the request is issued. The unique id will be issued by OB.
        public static final String X_FAPI_FINANCIAL_ID = "x-fapi-financial-id";
        // The time when the PSU last logged in with the TPP. All dates in the HTTP headers are represented as RFC 7231 Full Dates.
        public static final String X_FAPI_CUSTOMER_LAST_LOGGED_TIME = "x-fapi-customer-last-logged-time";
        // The PSU's IP address if the PSU is currently logged in with the TPP. This would be blank in case the PSU is not currently logged in.
        public static final String X_FAPI_CUSTOMER_IP_ADDRESS = "x-fapi-customer-ip-address";
        // An RFC4122 UID used as a correlation id. This id is used to track end-to-end interaction between TPP request and response sent by the API platform.
        public static final String X_FAPI_INTERACTION_ID = "x-fapi-interaction-id";
        public static final String X_JWS_SIGNATURE = "x-jws-signature";
    }

    public static final class Permissions {
        public static final String READ_ACCOUNT_BASIC = "ReadAccountsBasic";
        public static final String READ_ACCOUNTS_DETAIL = "ReadAccountsDetail";
        public static final String READ_BALANCES = "ReadBalances";
        public static final String READ_TRANSACTION_BASIC = "ReadTransactionsBasic";
        public static final String READ_TRANSACTIONS_DETAIL = "ReadTransactionsDetail";
        public static final String READ_TRANSCTIONS_CREDTIS = "ReadTransactionsCredits";
        public static final String READ_TRANSACTIONS_DEBITS = "ReadTransactionsDebits";

        public static final ImmutableList<String> ALL_READ_PERMISSIONS = ImmutableList
                .of(READ_ACCOUNT_BASIC, READ_ACCOUNTS_DETAIL, READ_BALANCES, READ_TRANSACTION_BASIC,
                        READ_TRANSACTIONS_DETAIL, READ_TRANSCTIONS_CREDTIS, READ_TRANSACTIONS_DEBITS);
    }

    public static final class Storage {
        public static final String STATE = "state";
        public static final String TOKEN = "token";
        public static final String ACCOUNT_ID = "accountId";
        public static final String ICS_CONFIGURATION = "icsConfiguration";
    }

    public static final class Date {
        public static final SimpleDateFormat TRANSACTION_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
        public static final SimpleDateFormat LAST_LOGGED_TIME_FORMAT = new SimpleDateFormat("EEE, dd MMMM yyyy HH:mm:ss z");
    }
}
