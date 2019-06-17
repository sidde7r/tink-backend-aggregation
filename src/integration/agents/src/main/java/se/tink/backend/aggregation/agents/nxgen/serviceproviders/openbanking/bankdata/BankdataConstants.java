package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata;

import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Constants;
import se.tink.backend.aggregation.nxgen.core.account.TypeMapper;

public final class BankdataConstants {

    public static final String INTEGRATION_NAME = "bankdata";
    public static final TypeMapper<AccountTypes> ACCOUNT_TYPE_MAPPER =
            TypeMapper.<AccountTypes>builder()
                    .put(AccountTypes.CHECKING, "checkings")
                    .put(AccountTypes.CHECKING, "savings")
                    .build();

    private BankdataConstants() {
        throw new AssertionError();
    }

    public static class ErrorMessages {
        public static final String INVALID_CONFIGURATION =
                "Invalid Configuration: %s cannot be empty or null";
        public static final String MISSING_CONFIGURATION = "Client Configuration missing.";
        public static final String MISSING_TOKEN = "Cannot find token.";
    }

    public static class Endpoints {
        public static final String AUTHORIZE = "/oauth-authorize";
        public static final String TOKEN = "/oauth-token";
        public static final String CONSENT = "/openbanking-consent/v1/consents";
        public static final String AUTHORIZE_CONSENT =
                "/openbanking-consent/v1/consents/{consentId}/authorisations";
        public static final String ACCOUNTS = "/openbanking-account/v1/accounts";
        public static final String AIS_PRODUCT = "/openbanking-account";
        public static final String TRANSACTIONS = "";
    }

    public static class StorageKeys {
        public static final String OAUTH_TOKEN = OAuth2Constants.PersistentStorageKeys.ACCESS_TOKEN;
        public static final String CODE_VERIFIER = "CODE_VERIFIER";
        public static final String CONSENT_ID = "consentId";
        public static final String IBAN = "IBAN";
        public static final String CLIENT_ID = "CLIENT_ID";
        public static final String ACCOUNT_ID = "ACCOUNT_ID";
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
        public static final String GRANT_TYPE = "grant_type";
        public static final String CODE = "code";
        public static final String REFRESH_TOKEN = "refresh_token";
        public static final String CODE_VERIFIER = "code_verifier";
        public static final String DATE_FROM = "dateFrom";
        public static final String DATE_TO = "dateTo";
    }

    public static class QueryValues {
        public static final String SCOPE = "ais:";
        public static final String CODE = "code";
        public static final String CODE_CHALLENGE_METHOD = "S256";
        public static final String TRUE = "true";
        public static final String BOOKED = "booked";
        public static final String DATE_FROM = "2000-10-10";
        public static final String BOTH = "both";
    }

    public static class HeaderKeys {
        public static final String X_API_KEY = "x-api-key";
        public static final String X_REQUEST_ID = "X-Request-ID";
        public static final String CONSENT_ID = "Consent-ID";
        public static final String CONTENT_TYPE = "Content-Type";
    }

    public static class FormValues {
        public static final String AUTHORIZATION_CODE = "authorization_code";
        public static final String REFRESH_TOKEN_GRANT_TYPE = "refresh_token";
        public static final String CLIENT_CREDENTIALS = "client_credentials";
        public static final String SCOPE = "aisprepare";
    }

    public static class IdTags {
        public static final String CONSENT_ID = "consentId";
    }

    public static class ConsentRequest {
        public static final String ALL_ACCOUNTS = "allAccounts";
    }

    public static class Accounts {
        public static final String BALANCE_FORWARD_AVAILABLE = "forwardAvailable";
    }

    public static class Format {
        public static final String TIMEZONE = "UTC";
        public static final String TIMESTAMP = "yyyy-MM-dd";
    }

    public static class CredentialKeys {
        public static final String IBAN = "iban";
    }
}
