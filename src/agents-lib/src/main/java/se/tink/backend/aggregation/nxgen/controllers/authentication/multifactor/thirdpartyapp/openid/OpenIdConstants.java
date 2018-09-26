package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid;

import java.util.Arrays;
import java.util.List;

public class OpenIdConstants {
    public static final List<String> SCOPES = Arrays.asList("openid", "accounts", "payments");
    public static final String[] RESPONSE_TYPES = { "code id_token" };
    public static final String[] GRANT_TYPES = {
            "authorization_code",
            "refresh_token",
            "client_credentials"
    };

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
