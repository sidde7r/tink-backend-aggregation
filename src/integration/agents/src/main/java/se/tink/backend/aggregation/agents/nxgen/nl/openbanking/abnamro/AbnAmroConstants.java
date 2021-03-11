package se.tink.backend.aggregation.agents.nxgen.nl.openbanking.abnamro;

import java.util.regex.Pattern;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.constants.OAuth2Constants.PersistentStorageKeys;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public final class AbnAmroConstants {

    public static final String DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss z";
    public static final String TRANSACTION_BOOKING_DATE_FORMAT = "yyyy-MM-dd";
    public static final Pattern JOINT_ACCOUNT_SUFFIX_PATTERN =
            Pattern.compile(" CJ$", Pattern.CASE_INSENSITIVE);

    private AbnAmroConstants() {
        throw new AssertionError();
    }

    public static class URLs {
        public static final URL OAUTH2_ABNAMRO_SSL =
                new URL("https://auth.connect.abnamro.com:8443");
        public static final URL OAUTH2_TOKEN_ABNAMRO =
                OAUTH2_ABNAMRO_SSL.concatWithSeparator("as").concatWithSeparator("token.oauth2");
        private static final URL API_ABNAMRO = new URL("https://api.abnamro.com");
        public static final URL ABNAMRO_CONSENT_INFO =
                API_ABNAMRO.concatWithSeparator("v1").concatWithSeparator("consentinfo");
        public static final URL ABNAMRO_ACCOUNTS =
                API_ABNAMRO.concatWithSeparator("v1").concatWithSeparator("accounts");
        private static final URL OAUTH2_ABNAMRO = new URL("https://auth.connect.abnamro.com");
        public static final URL AUTHORIZE_ABNAMRO =
                OAUTH2_ABNAMRO
                        .concatWithSeparator("as")
                        .concatWithSeparator("authorization.oauth2");
        private static final String ACCOUNT_DETAILS = "details";
        private static final String BALANCES_SUFFIX = "balances";
        private static final String TRANSACTIONS_SUFFIX = "transactions";

        public static URL buildAccountHolderUrl(final String accountId) {
            return ABNAMRO_ACCOUNTS
                    .concatWithSeparator(accountId)
                    .concatWithSeparator(ACCOUNT_DETAILS);
        }

        public static URL buildBalanceUrl(final String accountId) {
            return ABNAMRO_ACCOUNTS
                    .concatWithSeparator(accountId)
                    .concatWithSeparator(BALANCES_SUFFIX);
        }

        public static URL buildTransactionsUrl(final String accountId) {
            return ABNAMRO_ACCOUNTS
                    .concatWithSeparator(accountId)
                    .concatWithSeparator(TRANSACTIONS_SUFFIX);
        }
    }

    public static class ErrorMessages {
        public static final String MISSING_CONFIGURATION = "Client Configuration missing.";
    }

    public class StorageKey {
        public static final String OAUTH_TOKEN = PersistentStorageKeys.OAUTH_2_TOKEN;
        public static final String ACCOUNT_ID = "accountId";
    }

    public class Signature {
        public static final String ALGORITHM = "algorithm";
        public static final String HEADERS = "headers";
        public static final String KEY_ID = "keyId";
        public static final String SHA_512 = "sha-512";
        public static final String SIGNING_STRING_DIGEST = "digest: ";
        public static final String SIGNING_STRING_DATE = "date: ";
        public static final String SIGNING_STRING_REQUEST_ID = "x-request-id: ";
        public static final String SIGNING_STRING_SHA_512 = SHA_512 + "=";
        public static final String SIGNATURE = "signature";
    }

    public class QueryParams {
        public static final String API_KEY = "API-Key";
        public static final String CLIENT_ID = "client_id";
        public static final String CODE = "code";
        public static final String GRANT_TYPE = "grant_type";
        public static final String RESPONSE_TYPE = "response_type";
        public static final String REDIRECT_URI = "redirect_uri";
        public static final String SCOPE = "scope";
        public static final String STATE = "state";
        public static final String BANK = "bank";
        public static final String FLOW = "flow";
        public static final String REFRESH_TOKEN = "refresh_token";
        public static final String BOOK_DATE_FROM = "bookDateFrom";
        public static final String BOOK_DATE_TO = "bookDateTo";
        public static final String NEXT_PAGE_KEY = "nextPageKey";
    }

    public class QueryValues {
        public static final String AUTHORIZATION_CODE = "authorization_code";
        public static final String REFRESH_TOKEN = "refresh_token";
        public static final String CODE = "code";
        public static final String SCOPES =
                "psd2:account:balance:read psd2:account:transaction:read psd2:account:details:read";
        public static final String NLAA01 = "NLAA01";
    }
}
