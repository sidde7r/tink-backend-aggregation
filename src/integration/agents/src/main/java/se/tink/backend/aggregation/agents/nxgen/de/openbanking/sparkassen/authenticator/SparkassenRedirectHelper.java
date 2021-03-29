package se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator;

import java.util.Base64;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.errors.ThirdPartyAppError;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.SparkassenApiClient;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.SparkassenStorage;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.rpc.OauthEndpointsResponse;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ConsentResponse;
import se.tink.backend.aggregation.api.Psd2Headers;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Authenticator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.form.Form;
import se.tink.backend.aggregation.nxgen.http.url.URL;

@RequiredArgsConstructor
public class SparkassenRedirectHelper implements OAuth2Authenticator {

    private static final String AUTHORIZATION_CODE = "authorization_code";
    private static final String CLIENT_ID = "client_id";
    private static final String CODE = "code";
    private static final String CODE_CHALLENGE = "code_challenge";
    private static final String CODE_CHALLENGE_METHOD = "code_challenge_method";
    private static final String CODE_VERIFIER = "code_verifier";
    private static final String GRANT_TYPE = "grant_type";
    private static final String METHOD_VALUE = "S256";
    private static final String REFRESH_TOKEN = "refresh_token";
    private static final String RESPONSE_TYPE = "response_type";
    private static final String SCOPE = "scope";
    private static final String SCOPE_AIS = "AIS:";
    private static final String STATE = "state";

    private final RandomValueGenerator randomValueGenerator;
    private final SparkassenStorage storage;
    private final SparkassenApiClient apiClient;
    private final String clientId;

    @Override
    public URL buildAuthorizeUrl(String state) {
        ConsentResponse consent = apiClient.createConsent();
        storage.saveConsentId(consent.getConsentId());
        String scaOAuthUrl = consent.getLinks().getScaOAuth().getHref();

        OauthEndpointsResponse oauthEndpointsResponse = apiClient.getOauthEndpoints(scaOAuthUrl);
        storage.saveTokenEndpoint(oauthEndpointsResponse.getTokenEndpoint());

        String codeVerifier = generateCodeVerifier();
        storage.saveCodeVerifier(codeVerifier);
        String codeChallenge = Psd2Headers.generateCodeChallenge(codeVerifier);

        return new URL(oauthEndpointsResponse.getAuthorizationEndpoint())
                .queryParam(RESPONSE_TYPE, CODE)
                .queryParam(CLIENT_ID, clientId)
                .queryParam(SCOPE, SCOPE_AIS + consent.getConsentId())
                .queryParam(STATE, state)
                .queryParam(CODE_CHALLENGE, codeChallenge)
                .queryParam(CODE_CHALLENGE_METHOD, METHOD_VALUE);
    }

    private String generateCodeVerifier() {
        final byte[] code = randomValueGenerator.secureRandom(43);
        return Base64.getEncoder().withoutPadding().encodeToString(code);
    }

    @Override
    public OAuth2Token exchangeAuthorizationCode(String code) {
        String tokenEntity =
                Form.builder()
                        .put(CODE, code)
                        .put(CLIENT_ID, clientId)
                        .put(CODE_VERIFIER, storage.getCodeVerifier())
                        .put(GRANT_TYPE, AUTHORIZATION_CODE)
                        .build()
                        .serialize();

        return apiClient.sendToken(storage.getTokenEndpoint(), tokenEntity).toTinkToken();
    }

    @Override
    public OAuth2Token refreshAccessToken(String refreshToken) {
        String tokenEntity =
                Form.builder()
                        .put(GRANT_TYPE, REFRESH_TOKEN)
                        .put(REFRESH_TOKEN, refreshToken)
                        .build()
                        .serialize();
        OAuth2Token oAuth2Token =
                apiClient.sendToken(storage.getTokenEndpoint(), tokenEntity).toTinkToken();
        if (oAuth2Token.isRefreshNullOrEmpty()) {
            oAuth2Token.setRefreshToken(refreshToken);
        }
        return oAuth2Token;
    }

    @Override
    public void useAccessToken(OAuth2Token accessToken) {
        storage.saveToken(accessToken);
    }

    @Override
    public void handleSpecificCallbackDataError(Map<String, String> callbackData)
            throws AuthenticationException {
        if (callbackData.keySet().size() == 1 && callbackData.containsKey(STATE)) {
            // Only state returned, this is from not-OK path, when user clicks "cancel"
            throw ThirdPartyAppError.CANCELLED.exception();
        }
    }
}
