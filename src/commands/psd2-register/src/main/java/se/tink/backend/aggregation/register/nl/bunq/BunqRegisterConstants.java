package se.tink.backend.aggregation.register.nl.bunq;

import se.tink.backend.aggregation.nxgen.core.account.GenericTypeMapper;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.register.RegisterEnvironment;

public final class BunqRegisterConstants {

    private BunqRegisterConstants() {}

    public static final class Urls {
        public static final String REGISTER_AS_PSD2_PROVIDER =
                "/v1/payment-service-provider-credential";
        public static final String GET_OAUTH_CLIENT_ID = "/v1/user/{userId}/oauth-client";
        public static final String GET_CLIENT_ID_AND_SECRET =
                "/v1/user/{userId}/oauth-client/{oauth-clientId}";
        public static final String REGISTER_CALLBACK_URL =
                "/v1/user/{userId}/oauth-client/{oauth-clientId}/callback-url";
        public static final String DELETE_SESSION = "/v1/session/{itemId}";
    }

    public static final class Endpoints {
        public static final String PRODUCTION = "https://api.bunq.com";
        public static final String SANDBOX = "https://public-api.sandbox.bunq.com";
        public static final URL AUTHORIZE = new URL("https://oauth.sandbox.bunq.com/auth");
    }

    public static final class UrlParameterKeys {
        public static final String OAUTH_CLIENT_ID = "oauth-clientId";
        public static final String ITEM_ID = "itemId";
    }

    public static final class Mappers {
        public static final GenericTypeMapper<String, RegisterEnvironment>
                environmentOptionToApiEndpointMapper =
                        GenericTypeMapper.<String, RegisterEnvironment>genericBuilder()
                                .put(Endpoints.PRODUCTION, RegisterEnvironment.PRODUCTION)
                                .put(Endpoints.SANDBOX, RegisterEnvironment.SANDBOX)
                                .build();
    }
}
