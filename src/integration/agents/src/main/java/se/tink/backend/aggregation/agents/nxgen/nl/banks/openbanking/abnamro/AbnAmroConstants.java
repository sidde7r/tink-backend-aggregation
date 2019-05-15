package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.abnamro;

import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Constants;
import se.tink.backend.aggregation.nxgen.http.URL;

public final class AbnAmroConstants {

    private AbnAmroConstants() {
        throw new AssertionError();
    }

    public static final String DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss z";
    public static final String TRANSACTION_BOOKING_DATE_FORMAT = "yyyy-MM-dd";
    public static final int START_PAGE = 1;
    public static final String INTEGRATION_NAME = "abnamro";

    public static class URLs {
        private static final URL API_ABNAMRO = new URL("https://api-sandbox.abnamro.com");
        private static final URL OAUTH2_ABNAMRO =
                new URL("https://auth-sandbox.connect.abnamro.com");
        public static final URL OAUTH2_ABNAMRO_SSL =
                new URL("https://auth-sandbox.connect.abnamro.com:8443");
        public static final URL AUTHORIZE_ABNAMRO =
                OAUTH2_ABNAMRO
                        .concatWithSeparator("as")
                        .concatWithSeparator("authorization.oauth2");
        public static final URL OAUTH2_TOKEN_ABNAMRO =
                OAUTH2_ABNAMRO_SSL.concatWithSeparator("as").concatWithSeparator("token.oauth2");
        public static final URL ABNAMRO_CONSENT_INFO =
                API_ABNAMRO.concatWithSeparator("v1").concatWithSeparator("consentinfo");
        public static final URL ABNAMRO_ACCOUNTS =
                API_ABNAMRO.concatWithSeparator("v1").concatWithSeparator("accounts");
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
        public static final String INVALID_CONFIGURATION =
                "Invalid Configuration: %s cannot be empty or null";
        public static final String MISSING_CONFIGURATION = "Client Configuration missing.";
    }

    public class StorageKey {
        public static final String OAUTH_TOKEN = OAuth2Constants.PersistentStorageKeys.ACCESS_TOKEN;
        public static final String RESOURCE_ID = "resource_id";
        public static final String ACCOUNT_CONSENT_ID = "ConsentAccountId";
    }

    public class Signature {
        public static final String ALGORITHM = "algorithm";
        public static final String HEADERS = "headers";
        public static final String KEY_ID = "keyId";
        public static final String SHA_512 = "sha-512";
        public static final String RSA_SHA_512 = "rsa-sha512";
        public static final String SIGNING_STRING_DIGEST = "digest: ";
        public static final String SIGNING_STRING_DATE = "date: ";
        public static final String SIGNING_STRING_REQUEST_ID = "x-request-id: ";
        public static final String SIGNING_STRING_SHA_512 = SHA_512 + "=";
        public static final String SIGNATURE = "signature";
        public static final String HEADERS_VALUE = "date digest x-request-id";
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
        public static final String REQUEST_ID = "X-Request-ID";
        public static final String CONTENT_TYPE = "Content-Type";
        public static final String CONSENT_ID = "Consent-ID";
        public static final String AUTHORIZATION = "Authorization";
        public static final String TPP_SIGNATURE_CERTIFICATE = "TPP-Signature-Certificate";
        public static final String SIGNATURE = "Signature";
        public static final String DATE = "Date";
        public static final String PAGE = "page";
        public static final String BOOK_DATE_FROM = "bookDateFrom";
        public static final String BOOK_DATE_TO = "bookDateTo";
    }

    public class QueryValues {
        public static final String AUTHORIZATION_CODE = "authorization_code";
        public static final String CODE = "code";
        public static final String SCOPES =
                "psd2:account:balance:read psd2:account:transaction:read psd2:account:details:read";
        public static final String NLAA01 = "NLAA01";
    }
}
