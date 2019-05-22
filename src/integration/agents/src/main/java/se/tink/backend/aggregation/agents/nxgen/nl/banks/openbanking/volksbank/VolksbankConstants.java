package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank;

import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.nxgen.core.account.TypeMapper;

public class VolksbankConstants {

    public static class Urls {
        public static final String HOST = "https://PSD.bancairediensten.nl";
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
    }

    public static class Paths {
        public static final String AUTHORIZE = "/v1/authorize";
        public static final String TOKEN = "/v1/token";
        public static final String ACCOUNTS = "/v1/accounts";
        public static final String BALANCES = "/balances";
        public static final String TRANSACTIONS = "/transactions";
        public static final String CONSENT = "/v1/consents";
    }

    public static class TransactionFetcherParams {
        public static final String BOOKING_STATUS = "bookingStatus";
        public static final String BOOKING_STATUS_VALUE = "both";
        public static final String DATE_FROM = "dateFrom";
        public static final String PAGE_DIRECTION = "pageDirection";
        public static final String PAGE_DIRECTION_VALUE = "previous";
        public static final String LIMIT = "limit";
        public static final Integer LIMIT_VALUE = 5;
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
    }

    public static class Market {
        public static final String INTEGRATION_NAME = "volksbank";
        public static final String CLIENT_NAME = "tink";
    }

    public static class ConsentParams {
        public static final Integer VALID_YEAR = 2;
        public static final Integer FREQUENCY_PER_DAY = 1;
    }

    public static class TokenParams {
        public static final String AUTHORIZATION_CODE = "authorization_code";
        public static final String REFRESH_TOKEN = "refresh_token";
    }
}
