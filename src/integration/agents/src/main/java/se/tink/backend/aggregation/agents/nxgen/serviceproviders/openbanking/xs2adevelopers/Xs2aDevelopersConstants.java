package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers;

import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Constants;
import se.tink.backend.aggregation.nxgen.core.account.TypeMapper;

public final class Xs2aDevelopersConstants {

    public static final String INTEGRATION_NAME = "crelan";
    public static final TypeMapper<AccountTypes> ACCOUNT_TYPE_MAPPER =
            TypeMapper.<AccountTypes>builder()
                    .put(AccountTypes.CHECKING, "SAC")
                    .put(AccountTypes.SAVINGS, "SAV")
                    .build();

    private Xs2aDevelopersConstants() {
        throw new AssertionError();
    }

    public static class ErrorMessages {
        public static final String INVALID_CONFIGURATION =
                "Invalid Configuration: %s cannot be empty or null";
        public static final String MISSING_CONFIGURATION = "Client Configuration missing.";
        public static final String MISSING_TOKEN = "Cannot find token.";
        public static final String UNKNOWN_ACCOUNT_TYPE = "Unknown account type.";
        public static final String MISSING_AUTHENTICATOR = "Cannot find authenticator.";
    }

    public static class ApiServices {
        public static final String POST_CONSENT = "/berlingroup/v1/consents";
        public static final String AUTHORIZE = "/public/berlingroup/authorize";
        public static final String TOKEN = "/berlingroup/v1/token";
        public static final String GET_ACCOUNTS = "/berlingroup/v1/accounts";
        public static final String GET_BALANCES = "/berlingroup/v1/accounts/{accountId}/balances";
        public static final String GET_TRANSACTIONS =
                "/berlingroup/v1/accounts/{accountId}/transactions";
    }

    public static class StorageKeys {
        public static final String OAUTH_TOKEN = OAuth2Constants.PersistentStorageKeys.ACCESS_TOKEN;
        public static final String CONSENT_ID = "consent_id";
    }

    public static class QueryKeys {
        public static final String STATE = "state";
        public static final String REDIRECT_URI = "redirect_uri";
        public static final String CLIENT_ID = "client_id";
        public static final String SCOPE = "scope";
        public static final String CODE_CHALLENGE = "code_challenge";
        public static final String RESPONSE_TYPE = "response_type";
        public static final String CODE_CHALLENGE_TYPE = "code_challenge_type";
        public static final String DATE_FROM = "dateFrom";
        public static final String DATE_TO = "dateTo";
        public static final String BOOKING_STATUS = "bookingStatus";
    }

    public static class QueryValues {
        public static final String BOTH = "both";
        public static final String CODE = "code";
        public static final String CODE_CHALLENGE_TYPE = "S256";
        public static final String CODE_CHALLENGE = "dBjftJeZ4CVP-mB92K27uhbUJU1p1r_wW1gFWFOEjXk";
        public static final String PSU_IP_ADDRESS = "127.0.0.1";
    }

    public static class HeaderKeys {
        public static final String AUTHORIZATION = "Authorization";
        public static final String CONSENT_ID = "Consent-Id";
        public static final String TPP_REDIRECT_URI = "TPP-Redirect-URI";
        public static final String PSU_IP_ADDRESS = "PSU-IP-ADDRESS";
        public static final String X_REQUEST_ID = "X-Request-ID";
    }

    public static class FormKeys {
        public static final String GRANT_TYPE = "grant_type";
        public static final String CODE = "code";
        public static final String REDIRECT_URI = "redirect_uri";
        public static final String VALID_REQUEST = "valid_request";
        public static final String CODE_VERIFIER = "code_verifier";
        public static final String CLIENT_ID = "client_id";
        public static final String REFRESH_TOKEN = "refresh_token";
    }

    public static class FormValues {
        public static final String AUTHORIZATION_CODE = "authorization_code";
        public static final String CODE_VERIFIER = "code_verifier";
        public static final String REFRESH_TOKEN = "refresh_token";
        public static final Boolean FALSE = false;
        public static final int FREQUENCY_PER_DAY = 4;
        public static final Boolean TRUE = true;
        public static final String VALID_UNTIL = "2020-11-11";
        public static final String EUR = "EUR";
    }

    public static class IdTags {
        public static final String ACCOUNT_ID = "accountId";
    }

    public static class BalanceTypes {
        public static final String INTERIM_AVAILABLE = "interimAvailable";
    }

    public static class CredentialKeys {
        public static final String IBAN = "iban";
    }
}
