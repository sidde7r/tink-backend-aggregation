package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeaopenbanking.v2;

import com.google.common.collect.ImmutableList;
import java.util.List;
import se.tink.backend.aggregation.nxgen.core.account.AccountTypeMapper;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.rpc.AccountTypes;

public class NordeaBaseConstants {

    public static class Url {
        private static final String API_BASE_URL = "https://api.nordeaopenbanking.com";
        private static final String API_VERSION = "/v2/";

        public static final URL ACCOUNTS = new URL(API_BASE_URL + API_VERSION + "accounts");
        public static final URL AUTHORIZE_DECOUPLED = new URL(API_BASE_URL + API_VERSION + "authorize-decoupled");
        public static final URL AUTHORIZE = new URL(API_BASE_URL + API_VERSION + "authorize");
        public static final URL ACCESS_TOKEN = new URL(API_BASE_URL + API_VERSION + "authorize/access_token");

        public static URL getUrlForLink(String path) {
            return new URL(API_BASE_URL + path);
        }

        public static String getTransactionPathForAccount(String accountId) {
            return String.format("%saccounts/%s/transactions", API_VERSION, accountId);
        }
    }

    public static class Header {
        public static final String CLIENT_ID = "X-IBM-Client-ID";
        public static final String CLIENT_SECRET = "X-IBM-Client-Secret";
    }

    public static class Query {
        public static final String CLIENT_ID = "client_id";
        public static final String STATE = "state";
        public static final String REDIRECT_URI = "redirect_uri";
        public static final String SCOPE = "scope";
        public static final String DURATION = "duration";
        public static final String LANGUAGE = "language";
        public static final String CODE = "code";
        public static final String ACCOUNTS = "accounts";
    }

    public static class Storage {
        public static final String ACCESS_TOKEN = "AccessToken";
        public static final String CLIENT_ID = "ClientId";
        public static final String CLIENT_SECRET = "ClientSecret";
        public static final String REDIRECT_URL = "RedirectUrl";
        public static final String TRANSACTIONS = "Transactions";
        public static final String LANGUAGE = "language";
        public static final String CODE = "code";
        public static final String COUNTRY = "country";
        public static final String ACCOUNTS = "accounts";
    }

    public static class Link {

        public static final String TRANSACTIONS_LINK = "transactions";

        public static final String NEXT_LINK = "next";
        public static final String POLL_AUTH_LINK = "order";
        public static final String TOKEN_LINK = "code";
    }

    public static class Authorization {
        private static final String DEFAULT_TOKEN_TYPE = "Bearer";

        public static final String STATE_COMPLETED = "COMPLETED";

        public static final String GRANT_TYPE_AUTH_CODE = "authorization_code";
        public static final String RESPONSE_TYPE = "nordea_code";
        public static final long TOKEN_DURATION = 129600;
        public static final String LANGUAGE = "en";

        public static final List<String> SCOPES =
                ImmutableList.of("ACCOUNTS_BASIC","ACCOUNTS_BALANCES","ACCOUNTS_DETAILS",
                        "ACCOUNTS_TRANSACTIONS","PAYMENTS_MULTIPLE");

        public static String tokenToAuthorizationValue(String token) {
            return String.format("%s %s", DEFAULT_TOKEN_TYPE, token);
        }
    }

    public static class Account {
        public static final String CLOSED = "CLOSED";

        public static final String ACCOUNT_NUMBER_SE = "BBAN_SE";
        public static final String ACCOUNT_NUMBER_IBAN = "IBAN";
    }

    public static final AccountTypeMapper ACCOUNT_TYPE = AccountTypeMapper.builder()
            .put(AccountTypes.CHECKING, "Current").build();

    public static class Transaction {
        public static final String RESERVED = "reserved";
    }
}
