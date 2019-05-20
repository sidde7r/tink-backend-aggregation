package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.authenticator;

import java.util.Optional;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.RedsysApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.RedsysConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.RedsysConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.configuration.RedsysConfiguration;
import se.tink.backend.aggregation.agents.utils.random.RandomUtils;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Authenticator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class RedsysAuthenticator implements OAuth2Authenticator {
    private final RedsysApiClient apiClient;
    private final SessionStorage sessionStorage;
    private final RedsysConfiguration configuration;
    private final String codeVerifier;

    public RedsysAuthenticator(
            RedsysApiClient apiClient,
            SessionStorage sessionStorage,
            RedsysConfiguration configuration) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
        this.configuration = configuration;
        this.codeVerifier = generateCodeVerifier();
    }

    private RedsysConfiguration getConfiguration() {
        return Optional.ofNullable(configuration)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_CONFIGURATION));
    }

    @Override
    public URL buildAuthorizeUrl(String state) {
        return apiClient.getAuthorizeUrl(state, this.codeVerifier);
    }

    @Override
    public OAuth2Token exchangeAuthorizationCode(String code) {
        return apiClient.getToken(code, this.codeVerifier);
    }

    @Override
    public OAuth2Token refreshAccessToken(String refreshToken) throws SessionException {
        return apiClient.refreshToken(refreshToken);
    }

    @Override
    public void useAccessToken(OAuth2Token accessToken) {
        sessionStorage.put(StorageKeys.OAUTH_TOKEN, accessToken);
    }

    private String generateCodeVerifier() {
        return RandomUtils.generateRandomBase64UrlEncoded(48);
    }
}
