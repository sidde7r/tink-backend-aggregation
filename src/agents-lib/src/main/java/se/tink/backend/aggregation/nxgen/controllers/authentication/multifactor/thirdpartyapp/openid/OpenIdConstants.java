package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid;

import com.google.common.collect.ImmutableList;
import java.util.Arrays;
import java.util.List;

public class OpenIdConstants {
    public static final List<String> SCOPES = Arrays.asList("openid", "accounts", "payments");

    public enum SIGNING_ALGORITHM {
        RS256
    }

    public enum TOKEN_ENDPOINT_AUTH_METHODS {
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
            .add("id_token")
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

    public static final ImmutableList<String> PREFERRED_TOKEN_ENDPOINT_AUTH_METHODS = ImmutableList.<String>builder()
            .add(TOKEN_ENDPOINT_AUTH_METHODS.client_secret_post.toString())
            .add(TOKEN_ENDPOINT_AUTH_METHODS.private_key_jwt.toString())
            .add(TOKEN_ENDPOINT_AUTH_METHODS.client_secret_basic.toString())
            .build();


    public static class ClaimParams {
        public static final String SOFTWARE_ID = "software_id";
        public static final String REDIRECT_URIS = "redirect_uris";
        public static final String SOFTWARE_STATEMENT = "software_statement";
        public static final String SCOPE = "scope";
        public static final String GRANT_TYPES = "grant_types";
        public static final String RESPONSE_TYPES = "response_types";
        public static final String TOKEN_ENDPOINT_AUTH_METHOD = "token_endpoint_auth_method";
        public static final String ID_TOKEN_SIGNED_RESPONSE_ALG = "id_token_signed_response_alg";
        public static final String TOKEN_ENDPOINT_AUTH_SIGNING_ALG = "token_endpoint_auth_signing_alg";
        public static final String REQUEST_OBJECT_SIGNING_ALG = "request_object_signing_alg";
        public static final String APPLICATION_TYPE = "application_type";
    }

    public static class ClaimDefaults{
        public static final String WEB = "web";
    }
}
