package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank;

import java.text.SimpleDateFormat;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.nxgen.core.account.TypeMapper;

public class VolksbankConstants {

    public static class Urls {
        public static final String HOST = "https://psd.bancairediensten.nl";
        public static final String BASE_PATH = "/psd2/";

        // note: this is placed between BASE_PATH and Paths, for production we only use
        // BASE_PATH/Paths
        public static final String SANDBOX_PATH = "/sandbox";
    }

    public static class QueryParams {
        public static final String CODE = "code";
        public static final String SCOPE = "scope";
        public static final String SCOPE_VALUE = "AIS";
        public static final String RESPONSE_TYPE = "response_type";
        public static final String RESPONSE_TYPE_VALUE = "code";
        public static final String STATE = "state";
        public static final String REDIRECT_URI = "redirect_uri";
        public static final String CLIENT_ID = "client_id";
        public static final String CLIENT_SECRET = "client_secret";
        public static final String GRANT_TYPE = "grant_type";
        public static final String REFRESH_TOKEN = "refresh_token";
        public static final String CONSENT_ID = "consentId";
    }

    public static class Paths {
        public static final String AUTHORIZE = "/v1/authorize";
        public static final String TOKEN = "/v1/token";
        public static final String ACCOUNTS = "/v1/accounts";
        public static final String BALANCES = "/balances";
        public static final String TRANSACTIONS = "/transactions";
        public static final String CONSENT = "/v1/consents";
    }

    public static class Transaction {
        public static final int DEFAULT_HISTORY_DAYS = -730;
        public static final SimpleDateFormat TRANSACTION_DATE_FORMAT =
                new SimpleDateFormat("yyyy-MM-dd");
        public static final SimpleDateFormat ENTRY_REFERENCE_DATE_FORMAT =
                new SimpleDateFormat("yyyyMMdd");
    }

    public static class TransactionFetcherParams {
        public static final String BOOKING_STATUS = "bookingStatus";
        public static final String BOOKING_STATUS_VALUE = "booked";
        public static final String DATE_FROM = "dateFrom";
        public static final String DATE_TO = "dateTo";
        public static final String PAGE_DIRECTION = "pageDirection";
        public static final String PAGE_DIRECTION_VALUE = "next";
        public static final String LIMIT = "limit";
        public static final Integer LIMIT_VALUE = 1000;
    }

    public static class Storage {
        public static final String OAUTH_TOKEN = "OAUTH_TOKEN";
        public static final String CONSENT = "CONSENT";
    }

    public static final TypeMapper<AccountTypes> ACCOUNT_TYPE_MAPPER =
            TypeMapper.<AccountTypes>builder()
                    .put(AccountTypes.CHECKING, "Current")
                    .put(AccountTypes.SAVINGS, "Savings")
                    .build();

    public class HeaderKeys {
        public static final String REQUEST_ID = "X-Request-ID";
        public static final String CONTENT_TYPE = "Content-Type";
        public static final String CONSENT_ID = "Consent-ID";
        public static final String AUTHORIZATION = "Authorization";
        public static final String TPP_REDIRECT_URI = "TPP-Redirect-URI";
    }

    public static class ConsentParams {
        public static final Integer VALID_YEAR = 2;
        public static final Integer FREQUENCY_PER_DAY = 100;
        public static final boolean RECURRING_INDICATOR = true;
        public static final int REFRESH_TOKEN_EXPIRY_TIME = 90 * 24 * 3600;
    }

    public static class ErrorCodes {
        public static final String CONSENT_EXPIRED = "CONSENT_EXPIRED";
        public static final String CONSENT_INVALID = "CONSENT_INVALID";
        public static final String SERVICE_BLOCKED = "SERVICE_BLOCKED";
    }

    static class HttpClient {
        public static final int MAX_RETRIES = 5;
        public static final int READ_TIMEOUT_MILLISECONDS = 2000;
        public static final int RETRY_SLEEP_MILLISECONDS = 2000;
    }

    public static class TokenParams {
        public static final String AUTHORIZATION_CODE = "authorization_code";
        public static final String REFRESH_TOKEN = "refresh_token";
    }
}
