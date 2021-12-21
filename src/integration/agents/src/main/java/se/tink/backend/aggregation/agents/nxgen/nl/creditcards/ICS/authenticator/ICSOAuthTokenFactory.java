package se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.authenticator;

import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.nxgen.http.form.Form;

@RequiredArgsConstructor
public class ICSOAuthTokenFactory {

    private final String clientId;

    private final String clientSecret;

    private final String redirectUri;

    public String clientCredentialsToken() {
        return tokenBuilder()
                .put(FormKeys.SCOPE, FormValues.SCOPE_ACCOUNTS)
                .put(FormKeys.GRANT_TYPE, ICSOAuthGrantTypes.CLIENT_CREDENTIALS.toString())
                .build()
                .serialize();
    }

    public String consentAuthorizationToken(String authenticationCode) {
        return tokenBuilder()
                .put(FormKeys.SCOPE, FormValues.SCOPE_ACCOUNTS)
                .put(FormKeys.GRANT_TYPE, ICSOAuthGrantTypes.AUTHORIZATION_CODE.toString())
                .put(FormKeys.AUTHENTICATION_CODE, authenticationCode)
                .put(FormKeys.REDIRECT_URI, redirectUri)
                .build()
                .serialize();
    }

    public String refreshToken(String refreshToken) {
        return tokenBuilder()
                .put(FormKeys.GRANT_TYPE, ICSOAuthGrantTypes.REFRESH_TOKEN.toString())
                .put(FormKeys.REFRESH_TOKEN, refreshToken)
                .build()
                .serialize();
    }

    private Form.Builder tokenBuilder() {
        return Form.builder()
                .put(FormKeys.CLIENT_ID, clientId)
                .put(FormKeys.CLIENT_SECRET, clientSecret);
    }

    private static final class FormKeys {
        public static final String SCOPE = "scope";
        public static final String CLIENT_ID = "client_id";
        public static final String CLIENT_SECRET = "client_secret";
        public static final String GRANT_TYPE = "grant_type";
        public static final String REFRESH_TOKEN = "refresh_token";
        public static final String REDIRECT_URI = "redirect_uri";
        public static final String AUTHENTICATION_CODE = "code";
    }

    private static final class FormValues {
        public static final String SCOPE_ACCOUNTS = "accounts";
    }
}
