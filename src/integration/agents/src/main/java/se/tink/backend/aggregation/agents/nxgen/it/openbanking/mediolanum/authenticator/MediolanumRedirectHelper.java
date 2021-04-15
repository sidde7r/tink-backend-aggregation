package se.tink.backend.aggregation.agents.nxgen.it.openbanking.mediolanum.authenticator;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.mediolanum.MediolanumApiClient;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.mediolanum.MediolanumConfiguration;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.mediolanum.MediolanumStorage;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.mediolanum.authenticator.data.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.mediolanum.authenticator.data.UnauthorizedResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Authenticator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.form.Form;
import se.tink.backend.aggregation.nxgen.http.url.URL;

@RequiredArgsConstructor
@Slf4j
public class MediolanumRedirectHelper implements OAuth2Authenticator {

    private static final String AUTHORIZATION_CODE = "authorization_code";
    private static final String CLIENT_ID = "client_id";
    private static final String CLIENT_SECRET = "client_secret";
    private static final String CODE = "code";
    private static final String GRANT_TYPE = "grant_type";
    private static final String REDIRECT_URI = "redirect_uri";
    private static final String SCOPE = "scope";
    private static final String SCOPE_AIS = "aisp.base";
    private static final String STATE = "state";

    private final MediolanumStorage storage;
    private final MediolanumApiClient apiClient;
    private final MediolanumConfiguration configuration;

    @Override
    public URL buildAuthorizeUrl(String state) {
        UnauthorizedResponse unauthorizedResponse = apiClient.getRedirectUrl();
        return new URL(unauthorizedResponse.getResult().getUrl())
                .queryParam(CLIENT_ID, configuration.getClientId())
                .queryParam(CLIENT_SECRET, configuration.getClientSecret())
                .queryParam(REDIRECT_URI, configuration.getRedirectUrl())
                .queryParam(SCOPE, SCOPE_AIS)
                .queryParam(STATE, state);
    }

    @Override
    public OAuth2Token exchangeAuthorizationCode(String code) {
        String tokenEntity =
                Form.builder()
                        .put(CODE, code)
                        .put(CLIENT_ID, configuration.getClientId())
                        .put(CLIENT_SECRET, configuration.getClientSecret())
                        .put(REDIRECT_URI, configuration.getRedirectUrl())
                        .put(GRANT_TYPE, AUTHORIZATION_CODE)
                        .build()
                        .serialize();

        TokenResponse tokenResponse = apiClient.sendToken(tokenEntity);
        return tokenResponse.toTinkToken();
    }

    @Override
    public OAuth2Token refreshAccessToken(String refreshToken) {
        throw SessionError.SESSION_EXPIRED.exception();
    }

    @Override
    public void useAccessToken(OAuth2Token accessToken) {
        storage.saveToken(accessToken);
    }

    @Override
    public void handleSpecificCallbackDataError(Map<String, String> callbackData) {
        log.info(
                "Received callback with unhandled error?"
                        + String.join(" ", callbackData.keySet()));
    }
}
