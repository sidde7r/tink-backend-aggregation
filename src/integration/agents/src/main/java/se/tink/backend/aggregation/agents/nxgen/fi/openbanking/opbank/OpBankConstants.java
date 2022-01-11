package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank;

import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import lombok.experimental.UtilityClass;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccountTypeMapper;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.backend.aggregation.nxgen.http.url.URL;

@UtilityClass
public final class OpBankConstants {

    // OP Bank assumes all dates that we send are in Finland's time zone
    public static final ZoneId API_ZONE_ID = ZoneId.of("Europe/Helsinki");

    public static final TransactionalAccountTypeMapper ACCOUNT_TYPE_MAPPER =
            TransactionalAccountTypeMapper.builder()
                    .put(TransactionalAccountType.CHECKING, "Käyttötili", "CURRENT ACCOUNT")
                    .build();

    public static class ErrorMessages {
        public static final String INVALID_CONFIGURATION =
                "Invalid Configuration: %s cannot be empty or null";
        public static final String MISSING_CONFIGURATION = "Client Configuration missing.";
        public static final String SERVICE_UNAVAILABLE = "Service unavailable";
    }

    public static class Urls {
        public static final String BASE_URL = "https://mtls.apis.op.fi";
        public static final String OAUTH_TOKEN = BASE_URL + "/oauth/token";
        public static final String ACCOUNTS_AUTHORIZATION =
                BASE_URL + "/accounts-psd2/v1/authorizations";
        public static final String CREATE_SEPA_PAYMENT =
                BASE_URL + "/payments-psd2/v1/sepa-payments";
        public static final String VERIFY_SEPA_PAYMENT =
                BASE_URL + "/payments-psd2/v1/sepa-payments/";
        public static final String SUBMIT_SEPA_PAYMENT = CREATE_SEPA_PAYMENT;
        public static final URL GET_ACCOUNTS = new URL(BASE_URL + "/accounts-psd2/v1/accounts");
        public static final URL GET_ACCOUNT_TRANSACTIONS =
                new URL(GET_ACCOUNTS + "/{accountId}/transactions");
        public static final URL GET_CREDIT_CARDS = new URL(BASE_URL + "/accounts-psd2/v1/cards");
        public static final URL GET_CREDIT_CARD_TRANSACTIONS =
                new URL(GET_CREDIT_CARDS + "/{cardId}/transactions");

        public static final String AUTHORIZATION_URL = "https://authorize.op.fi/oauth/authorize";
    }

    public static class StorageKeys {
        public static final String URL = "URL";
        public static final String STATE = "State";
        public static final String CODE = "Code";
        public static final String ACCOUNT_ID = "accountId";
        public static final String CARD_ID = "cardId";
    }

    public static class IdTags {
        public static final String ACCOUNT_ID = "accountId";
        public static final String CARD_ID = "cardId";
    }

    public static class HeaderKeys {
        public static final String X_API_KEY = "x-api-key";
        public static final String AUTHORIZATION = "Authorization";
        public static final String X_FAPI_FINANCIAL_ID = "x-fapi-financial-id";
        public static final String X_CUSTOMER_USER_AGENT = "x-customer-user-agent";
        public static final String X_FAPI_CUSTOMER_IP_ADDRESS = "x-fapi-customer-ip-address";
        public static final String X_FAPI_INTERACTION_ID = "x-fapi-interaction-id";
        public static final String X_IDEMPOTENCY_KEY = "x-idempotency-key";
        public static final String X_JWS_SIGNATURE = "x-jws-signature";
    }

    public static class HeaderValues {
        public static final String TINK = "tink";
    }

    public static class TokenValues {
        public static final String RESPONSE_TYPE = "code";
        public static final String SCOPE_AIS = "openid accounts";
        public static final String SCOPE_PIS = "openid payments";
        public static final int MAX_AGE = 86400;
        public static final String ACCOUNTS = "accounts";
        public static final String PAYMENTS = "payments";
        public static final String ARC_VALUES = "urn:openbanking:psd2:sca";
    }

    public static class JWTHeaderValues {
        public static final String ALG = "RS256";
        public static final String TYPE = "JWT";
    }

    public static class AuthorizationKeys {
        public static final String REQUEST = "request";
        public static final String RESPONSE_TYPE = "response_type";
        public static final String CLIENT_ID = "client_id";
        public static final String SCOPE = "scope";
    }

    public static class AuthorizationValues {
        public static final String CODE = "code";
        public static final String OPENID_ACCOUNTS = "openid accounts";
        static final int MAX_TRANSACTION_HISTORY_LENGTH_IN_DAYS = 729;
        static final int MAX_CONSENT_LENGTH_IN_DAYS = 89;
        public static final String OPENID_PAYMENTS = "openid payments";
    }

    public static class RefreshTokenFormKeys {
        public static final String GRANT_TYPE = "grant_type";
        public static final String REFRESH_TOKEN = "refresh_token";
        public static final String CLIENT_ID = "client_id";
        public static final String CLIENT_SECRET = "client_secret";
        public static final String REDIRECT_URI = "redirect_uri";
        public static final int DEFAULT_TOKEN_LIFETIME = 45;
        public static final TemporalUnit DEFAULT_TOKEN_LIFETIME_UNIT = ChronoUnit.DAYS;
    }

    public static class RefreshTokenFormValues {
        public static final String CLIENT_CREDENTIALS = "client_credentials";
        public static final String AUTHORIZATION_CODE = "authorization_code";
    }

    public static class Filters {
        public static final int NUMBER_OF_RETRIES = 5;
        public static final long MS_TO_WAIT = 4000;
    }
}
