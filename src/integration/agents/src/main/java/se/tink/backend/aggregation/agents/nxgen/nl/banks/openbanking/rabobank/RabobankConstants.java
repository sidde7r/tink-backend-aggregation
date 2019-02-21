package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank;

import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Constants;
import se.tink.backend.aggregation.nxgen.http.URL;

public class RabobankConstants {
    public static final String DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss z";
    public static final String TRANSACTION_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    public static final String TRANSACTION_BOOKING_DATE_FORMAT = "yyyy-MM-dd";
    public static final int START_PAGE = 1;
    public static final String INTEGRATION_NAME = "rabobank";

    public static class URLs {
        private static final URL API_RABOBANK =
                new URL("https://api-sandbox.rabobank.nl/openapi/sandbox");
        public static final URL OAUTH2_RABOBANK = API_RABOBANK.concatWithSeparator("oauth2");
        public static final URL PAYMENTS_RABOBANK = API_RABOBANK.concatWithSeparator("payments");
        public static final URL AUTHORIZE_RABOBANK =
                OAUTH2_RABOBANK.concatWithSeparator("authorize");
        public static final URL OAUTH2_TOKEN_RABOBANK =
                OAUTH2_RABOBANK.concatWithSeparator("token");
        public static final URL ACCOUNT_INFORMATION =
                PAYMENTS_RABOBANK
                        .concatWithSeparator("account-information")
                        .concatWithSeparator("ais")
                        .concatWithSeparator("v3");
        public static final URL AIS_RABOBANK_ACCOUNTS =
                ACCOUNT_INFORMATION.concatWithSeparator("accounts");
        private static final String BALANCES_SUFFIX = "balances";
        private static final String TRANSACTIONS_SUFFIX = "transactions";

        public static URL buildBalanceUrl(final String accountId) {
            return AIS_RABOBANK_ACCOUNTS
                    .concatWithSeparator(accountId)
                    .concatWithSeparator(RabobankConstants.URLs.BALANCES_SUFFIX);
        }

        public static URL buildTransactionsUrl(final String accountId) {
            return AIS_RABOBANK_ACCOUNTS
                    .concatWithSeparator(accountId)
                    .concatWithSeparator(TRANSACTIONS_SUFFIX);
        }
    }

    public class StorageKey {
        public static final String CLIENT_ID = "client_id";
        public static final String CLIENT_SECRET = "client_secret";
        public static final String OAUTH_TOKEN = OAuth2Constants.PersistentStorageKeys.ACCESS_TOKEN;
        public static final String REDIRECT_URL = "redirect_url";
        public static final String CLIENT_CERT = "client_cert";
        public static final String CLIENT_SSL_P12 = "client_ssl_p12";
        public static final String CLIENT_CERT_KEY_PASSWORD = "client_cert_key_password";
        public static final String CLIENT_CERT_SERIAL = "client_cert_serial";
        public static final String RESOURCE_ID = "resource_id";
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
        public static final String IBM_CLIENT_ID = "x-ibm-client-id";
        public static final String CLIENT_ID = "client_id";
        public static final String CODE = "code";
        public static final String GRANT_TYPE = "grant_type";
        public static final String RESPONSE_TYPE = "response_type";
        public static final String REDIRECT_URI = "redirect_uri";
        public static final String SCOPE = "scope";
        public static final String STATE = "state";
        public static final String REFRESH_TOKEN = "refresh_token";
        public static final String TPP_SIGNATURE_CERTIFICATE = "TPP-Signature-Certificate";
        public static final String REQUEST_ID = "X-Request-ID";
        public static final String DIGEST = "Digest";
        public static final String SIGNATURE = "Signature";
        public static final String DATE = "Date";
        public static final String BOOKING_STATUS = "bookingStatus";
        public static final String DATE_FROM = "dateFrom";
        public static final String DATE_TO = "dateTo";
        public static final String SIZE = "size";
        public static final String PAGE = "page";
    }

    public class QueryValues {
        public static final String AUTHORIZATION_CODE = "authorization_code";
        public static final String CODE = "code";
        public static final String BOTH = "both";
        public static final String TRANSACTIONS_SIZE = "100";
        public static final String SCOPES = "ais.balances.read ais.transactions.read-90days ais.transactions.read-history";
    }
}
