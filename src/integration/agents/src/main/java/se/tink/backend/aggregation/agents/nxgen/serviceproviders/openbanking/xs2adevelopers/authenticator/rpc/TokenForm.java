package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.authenticator.rpc;

import lombok.Builder;
import se.tink.backend.aggregation.nxgen.http.form.AbstractForm;

@Builder
public class TokenForm extends AbstractForm {

    private static class Keys {
        private static final String GRANT_TYPE = "grant_type";
        private static final String CODE = "code";
        private static final String REDIRECT_URI = "redirect_uri";
        private static final String VALID_REQUEST = "valid_request";
        private static final String CODE_VERIFIER = "code_verifier";
        private static final String CLIENT_ID = "client_id";
        private static final String REFRESH_TOKEN = "refresh_token";
    }

    private String grantType;
    private String code;
    private String redirectUri;
    private String codeVerifier;
    private Boolean validRequest;
    private String clientId;
    private String refreshToken;

    private TokenForm(
            String grantType,
            String code,
            String redirectUri,
            String codeVerifier,
            Boolean validRequest,
            String clientId,
            String refreshToken) {
        put(Keys.CLIENT_ID, clientId);
        put(Keys.GRANT_TYPE, grantType);
        putIfNotNull(Keys.CODE, code);
        putIfNotNull(Keys.REDIRECT_URI, redirectUri);
        putIfNotNull(Keys.VALID_REQUEST, String.valueOf(validRequest));
        putIfNotNull(Keys.CODE_VERIFIER, codeVerifier);
        putIfNotNull(Keys.REFRESH_TOKEN, refreshToken);
    }

    private void putIfNotNull(String key, String value) {
        if (value != null) {
            put(key, value);
        }
    }
}
