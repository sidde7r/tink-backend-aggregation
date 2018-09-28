package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid;

import com.google.common.collect.ImmutableList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class OpenIdConstants {
    public static final List<String> SCOPES = Arrays.asList("openid", "accounts", "payments");

    public enum SIGNING_ALGORITHM {
        RS256
    }

    public enum TOKEN_ENDPOINT_AUTH_METHOD {
        client_secret_post,
        client_secret_basic,
        private_key_jwt
    }

    public static final ImmutableList<String> MANDATORY_GRANT_TYPES = ImmutableList.<String>builder()
            .add("client_credentials")
            .add("authorization_code")
            .build();

    public static final ImmutableList<String> MANDATORY_RESPONSE_TYPES = ImmutableList.<String>builder()
            .add("code")
//            .add("id_token") //TODO: Enable when we have fragment implemented or when response_mode=query has effect.
            .build();

    public static final ImmutableList<String> PREFERRED_ID_TOKEN_SIGNING_ALGORITHM = ImmutableList.<String>builder()
            .add(SIGNING_ALGORITHM.RS256.toString())
            .build();

    public static final ImmutableList<String> PREFERRED_TOKEN_ENDPOINT_SIGNING_ALGORITHM = ImmutableList.<String>builder()
            .add(SIGNING_ALGORITHM.RS256.toString())
            .build();

    public static final ImmutableList<String> PREFERRED_REQUEST_OBJECT_SIGNING_ALGORITHM = ImmutableList.<String>builder()
            .add(SIGNING_ALGORITHM.RS256.toString())
            .build();

    public static final ImmutableList<TOKEN_ENDPOINT_AUTH_METHOD> PREFERRED_TOKEN_ENDPOINT_AUTH_METHODS =
            ImmutableList.<TOKEN_ENDPOINT_AUTH_METHOD>builder()
                    .add(TOKEN_ENDPOINT_AUTH_METHOD.client_secret_post)
                    .add(TOKEN_ENDPOINT_AUTH_METHOD.private_key_jwt)
                    .add(TOKEN_ENDPOINT_AUTH_METHOD.client_secret_basic)
                    .build();

    public static final ImmutableList<String> ACCOUNT_PERMISSIONS = ImmutableList.<String>builder()
            .add("ReadAccountsDetail",
                    "ReadBalances",
                    "ReadBeneficiariesDetail",
                    "ReadDirectDebits",
                    "ReadProducts",
                    "ReadStandingOrdersDetail",
                    "ReadTransactionsCredits",
                    "ReadTransactionsDebits",
                    "ReadTransactionsDetail").build();

    public static class ApiServices {
        public static final String ACCOUNT_REQUESTS = "/account-requests";
        public static final String PAYMENTS = "/payments";
        public static final String PAYMENT_SUBMISSIONS = "/payment-submissions/";
    }

    public static class Params {
        public static final String CLIENT_ID = "client_id";
        public static final String SOFTWARE_ID = "software_id";
        public static final String REDIRECT_URIS = "redirect_uris";
        public static final String REDIRECT_URI = "redirect_uri";
        public static final String SOFTWARE_STATEMENT = "software_statement";
        public static final String SCOPE = "scope";
        public static final String GRANT_TYPES = "grant_types";
        public static final String RESPONSE_TYPE = "response_type";
        public static final String RESPONSE_TYPES = "response_types";
        public static final String TOKEN_ENDPOINT_AUTH_METHOD = "token_endpoint_auth_method";
        public static final String ID_TOKEN_SIGNED_RESPONSE_ALG = "id_token_signed_response_alg";
        public static final String TOKEN_ENDPOINT_AUTH_SIGNING_ALG = "token_endpoint_auth_signing_alg";
        public static final String REQUEST_OBJECT_SIGNING_ALG = "request_object_signing_alg";
        public static final String APPLICATION_TYPE = "application_type";
        public static final String STATE = "state";
        public static final String MAX_AGE = "max_age";
        public static final String CLAIMS = "claims";
        public static final String NONCE = "nonce";
        public static final String REQUEST = "request";
    }

    public static class ParamDefaults {
        public static final String WEB = "web";
        public static final String RESPONSE_MODE = "query";

    }

    public static class DevParams {
        public static final String TINK_IP = "158.174.56.4";
        public static final String LAST_LOGIN = "Tue, 11 Sep 2012 19:43:31 UTC";
    }

    public static final String CLIENT_ASSERTION_TYPE = "client_assertion_type=urn:ietf:params:oauth:client-assertion-type:jwt-bearer";

    // "To indiciate that secure customer authentication must be carried out as mandated by the PSD2 RTS"
    public static final String ACR_SECURE_AUTHENTICATION_RTS = "urn:openbanking:psd2:sca";

    // According to examples the max age is 24h
    public static final long MAX_AGE = TimeUnit.DAYS.toSeconds(1);

    public static class CallbackParams {
        public static final String CODE = "code";
        public static final String ID_TOKEN = "id_token";
    }

    public static class PersistentStorageKeys {
        public static final String AUTH_TOKEN = "open_id_auth_token";
    }

    public static class HttpHeaders {
        public static final String AUTHORIZATION = "Authorization";
        public static final String X_FAPI_FINANCIAL_ID = "x-fapi-financial-id";
        public static final String X_FAPI_CUSTOMER_LAST_LOGGED_TIME = "x-fapi-customer-last-logged-time";
        public static final String X_FAPI_CUSTOMER_IP_ADDRESS = "x-fapi-customer-ip-address";
        public static final String X_FAPI_INTERACTION_ID = "x-fapi-interaction-id";
    }
}
