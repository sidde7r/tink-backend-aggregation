package se.tink.backend.aggregation.agents.nxgen.nl.openbanking.knab.authenticator.rpc;

import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.knab.KnabAuthorizationCredentials;
import se.tink.backend.aggregation.nxgen.http.form.Form;

@RequiredArgsConstructor
public class TokenRequestFactory {

    private static final String CLIENT_ID = "client_id";

    private static final String CLIENT_SECRET = "client_secret";

    private static final String GRANT_TYPE = "grant_type";

    private final KnabAuthorizationCredentials authorizationCredentials;

    private final String redirectUri;

    public String applicationAccessTokenRequest() {
        return Form.builder()
                .put(GRANT_TYPE, "client_credentials")
                .put("scope", "psd2")
                .build()
                .serialize();
    }

    public String accessTokenRequest(String code, String state) {
        return Form.builder()
                .put(GRANT_TYPE, "authorization_code")
                .put(CLIENT_ID, authorizationCredentials.getClientId())
                .put(CLIENT_SECRET, authorizationCredentials.getClientSecret())
                .put("code", code)
                .put("state", state)
                .put("redirect_uri", redirectUri)
                .build()
                .serialize();
    }

    public String refreshTokenRequest(String refreshToken) {
        return Form.builder()
                .put(GRANT_TYPE, "refresh_token")
                .put(CLIENT_ID, authorizationCredentials.getClientId())
                .put(CLIENT_SECRET, authorizationCredentials.getClientSecret())
                .put("refresh_token", refreshToken)
                .build()
                .serialize();
    }
}
