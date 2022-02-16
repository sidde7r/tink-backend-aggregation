package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid;

import com.google.common.collect.ImmutableList;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.Arrays;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.entities.TokenEndpointAuthMethod;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.rpc.SigningAlgorithm;

public class OpenIdConstants {

    static final String NATIONWIDE_ORG_ID = "0015800000jf8aKAAQ";

    public static class Scopes {
        public static final String OPEN_ID = "openid";
        public static final String ACCOUNTS = "accounts";
        public static final String PAYMENTS = "payments";
        public static final String OFFLINE_ACCESS = "offline_access";
    }

    public static final ImmutableList<String> RESPONSE_TYPES_FOR_HYBRID_FLOW_WITH_ID_TOKEN =
            ImmutableList.<String>builder()
                    .add(CallbackParams.CODE)
                    .add(CallbackParams.ID_TOKEN)
                    .build();

    public static final ImmutableList<String>
            RESPONSE_TYPES_FOR_HYBRID_FLOW_WITH_ID_TOKEN_AND_TOKEN =
                    ImmutableList.<String>builder()
                            .add(CallbackParams.CODE)
                            .add(CallbackParams.TOKEN)
                            .add(CallbackParams.ID_TOKEN)
                            .build();

    public static final List<SigningAlgorithm> PREFERRED_ID_TOKEN_SIGNING_ALGORITHM =
            Arrays.asList(SigningAlgorithm.PS256, SigningAlgorithm.RS256);

    static final ImmutableList<TokenEndpointAuthMethod> PREFERRED_TOKEN_ENDPOINT_AUTH_METHODS =
            ImmutableList.<TokenEndpointAuthMethod>builder()
                    .add(TokenEndpointAuthMethod.CLIENT_SECRET_POST)
                    .add(TokenEndpointAuthMethod.CLIENT_SECRET_BASIC)
                    .add(TokenEndpointAuthMethod.PRIVATE_KEY_JWT)
                    .add(TokenEndpointAuthMethod.TLS_CLIENT_AUTH)
                    .build();

    public static class Params {
        public static final String CLIENT_ID = "client_id";
        public static final String SOFTWARE_ID = "software_id";
        public static final String ORG_ID = "org_id";
        public static final String REDIRECT_URI = "redirect_uri";
        public static final String SCOPE = "scope";
        public static final String RESPONSE_TYPE = "response_type";
        public static final String STATE = "state";
        public static final String NONCE = "nonce";
        public static final String SOFTWARE_JWKS_ENDPOINT = "software_jwks_endpoint";
        public static final String ORG_JWKS_ENDPOINT = "org_jwks_endpoint";
    }

    public static final String CLIENT_ASSERTION_TYPE =
            "urn:ietf:params:oauth:client-assertion-type:jwt-bearer";

    public static class CallbackParams {
        public static final String CODE = "code";
        public static final String ID_TOKEN = "id_token";
        public static final String TOKEN = "token";
        public static final String ERROR = "error";
        public static final String ERROR_DESCRIPTION = "error_description";
    }

    public static class PersistentStorageKeys {
        public static final String AIS_ACCESS_TOKEN = "open_id_ais_access_token";
        public static final String AIS_ACCOUNT_CONSENT_ID = "ais_account_consent_id";
        static final String LAST_SCA_TIME = "last_SCA_time";
    }

    public static class HttpHeaders {
        public static final String AUTHORIZATION = "Authorization";
        public static final String X_FAPI_FINANCIAL_ID = "x-fapi-financial-id";
        public static final String X_FAPI_INTERACTION_ID = "x-fapi-interaction-id";
    }

    public static class Errors {
        public static final String ACCESS_DENIED = "access_denied";
        public static final String LOGIN_REQUIRED = "login_required";
        public static final String SERVER_ERROR = "server_error";
        public static final String TEMPORARILY_UNAVAILABLE = "temporarily_unavailable";
        public static final String INVALID_GRANT = "invalid_grant";
        public static final String UNAUTHORISED = "401 Unauthorised";
        public static final String INVALID_INTENT_ID = "invalid_openbanking_intent_id";
    }

    public static class ErrorDescriptions {
        public static final String SERVER_ERROR_PROCESSING = "server_error_processing";
    }

    public static class ErrorCodes {
        public static final String NOT_FOUND = "UK.OBIE.Resource.NotFound";
        public static final String REAUTHENTICATE = "UK.OBIE.Reauthenticate";
        public static final String INVALID_CONSENT_STATUS = "UK.OBIE.Resource.InvalidConsentStatus";
    }

    public static class Ps256 {

        public static class PayloadClaims {
            public static final String ISSUER = "iss";
            public static final String AUDIENCE = "aud";
        }
    }

    public static class Token {
        public static final int DEFAULT_TOKEN_LIFETIME = 90;
        public static final TemporalUnit DEFAULT_TOKEN_LIFETIME_UNIT = ChronoUnit.DAYS;
        public static final String CODE_AND_STATE_MSG = "code and state";
        public static final String ACCESS_TOKEN_MSG = "access token"; // pragma: allowlist secret;
        public static final String CLIENT_ACCESS_TOKEN_MSG =
                "client access token"; // pragma: allowlist secret;
    }
}
