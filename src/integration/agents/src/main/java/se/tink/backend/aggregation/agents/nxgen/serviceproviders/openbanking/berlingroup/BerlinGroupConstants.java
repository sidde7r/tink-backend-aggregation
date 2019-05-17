package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup;

import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Constants;
import se.tink.backend.aggregation.nxgen.core.account.TypeMapper;

public abstract class BerlinGroupConstants {

    public static final String CLIENT_NAME = "tink";
    public static final TypeMapper<AccountTypes> ACCOUNT_TYPE_MAPPER =
            TypeMapper.<AccountTypes>builder()
                    .put(
                            AccountTypes.CHECKING,
                            "CACC",
                            "CASH",
                            "CHAR",
                            "CISH",
                            "COMM",
                            "SLRY",
                            "TRAN",
                            "TRAS",
                            "CurrentAccount")
                    .put(AccountTypes.SAVINGS, "LLSV", "ONDP", "SVGS")
                    .build();

    private BerlinGroupConstants() {
        throw new AssertionError();
    }

    public static class Signature {
        public static final String DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss 'GMT'";
        public static final String TIMEZONE = "GMT";
        public static final String DIGEST = "digest: ";
        public static final String DIGEST_PREFIX = "SHA-256=";
        public static final String KEY_ID_NAME = "keyId=";
        public static final String SIGNING_ALGORITHM = "RSA";
        public static final String SIGNATURE_NAME = "signature=";
    }

    public static class StorageKeys {
        public static final String BASE_URL = "BASE_URL";
        public static final String OAUTH_TOKEN = OAuth2Constants.PersistentStorageKeys.ACCESS_TOKEN;
        public static final String CLIENT_ID = "CLIENT_ID";
        public static final String CLIENT_SECRET = "CLIENT_SECRET";
        public static final String IBAN = "IBAN";
        public static final String CODE_VERIFIER = "CODE_VERIFIER";
        public static final String CONSENT_ID = "consentId";
        public static final String TRANSACTIONS_URL = "TRANSACTIONS_URL";
    }

    public static class QueryKeys {
        public static final String CLIENT_ID = "client_id";
        public static final String SCOPE = "scope";
        public static final String RESPONSE_TYPE = "response_type";
        public static final String STATE = "state";
        public static final String REDIRECT_URI = "redirect_uri";
        public static final String CODE_CHALLENGE = "code_challenge";
        public static final String CODE_CHALLENGE_METHOD = "code_challenge_method";
        public static final String WITH_BALANCE = "withBalance";
        public static final String BOOKING_STATUS = "bookingStatus";
        public static final String CLIENT_SECRET = "client_secret";
        public static final String GRANT_TYPE = "grant_type";
        public static final String CODE = "code";
        public static final String REFRESH_TOKEN = "refresh_token";
    }

    public static class QueryValues {
        public static final String SCOPE = "AIS:";
        public static final String RESPONSE_TYPE = "code";
        public static final String CODE_CHALLENGE_METHOD = "S256";
        public static final String BOTH = "both";
        public static final String GRANT_TYPE = "authorization_code";
        public static final String GRANT_TYPE_REFRESH = "refresh_token";
        public static final String TRUE = "true";
    }

    public static class HeaderKeys {
        public static final String CONSENT_ID = "Consent-ID";
        public static final String X_REQUEST_ID = "X-Request-ID";
        public static final String WEB_API_KEY = "web-api-key";
        public static final String DIGEST = "Digest";
        public static final String DATE = "Date";
        public static final String TPP_SIGNATURE_CERTIFICATE = "TPP-Signature-Certificate";
        public static final String SIGNATURE = "Signature";
        public static final String TPP_REDIRECT_URI = "TPP-Redirect-Uri";
        public static final String SSL_CERTIFICATE = "SSL-Certificate";
        public static final String PSU_IP_ADDRESS = "PSU-IP-Address";
        public static final String AUTHORIZATION = "AUTHORIZATION";
    }

    public static class FormKeys {
        public static final String GRANT_TYPE = "grant_type";
        public static final String CODE = "code";
        public static final String REDIRECT_URI = "redirect_uri";
        public static final String CLIENT_ID = "client_id";
        public static final String CLIENT_SECRET = "client_secret";
        public static final String CODE_VERIFIER = "code_verifier";
        public static final String REFRESH_TOKEN = "refresh_token";
    }

    public static class FormValues {
        public static final String AUTHORIZATION_CODE = "authorization_code";
        public static final String CLIENT_CREDENTIALS = "client_credentials";
        public static final String REFRESH_TOKEN_GRANT_TYPE = "refresh_token";
        public static final String EMPTY = "";
    }

    public static class Accounts {
        public static final String BALANCE_CLOSING_BOOKED = "closingBooked";
    }

    public static class Certificate {
        public static final String UTF_8 = "UTF-8";
    }

    public static class ErrorMessages {
        public static final String INVALID_CONFIGURATION =
                "Invalid Configuration: %s cannot be empty or null";
        public static final String MISSING_CONFIGURATION = "Client Configuration missing.";
        public static final String MISSING_TOKEN = "Cannot find token.";
        public static final String ENCODE_CERTIFICATE_ERROR = "Cannot encode certificate.";
    }
}
