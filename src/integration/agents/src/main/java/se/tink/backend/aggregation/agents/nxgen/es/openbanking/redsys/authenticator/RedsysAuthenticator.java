package se.tink.backend.aggregation.agents.nxgen.es.openbanking.redsys.authenticator;

import java.util.Optional;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.redsys.RedsysApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.redsys.RedsysConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.redsys.RedsysConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.redsys.configuration.RedsysConfiguration;
import se.tink.backend.aggregation.agents.utils.random.RandomUtils;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Authenticator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class RedsysAuthenticator implements OAuth2Authenticator {

    private final RedsysApiClient apiClient;
    private final PersistentStorage persistentStorage;
    private final RedsysConfiguration configuration;
    private final String codeVerifier;

    public RedsysAuthenticator(
            RedsysApiClient apiClient,
            PersistentStorage persistentStorage,
            RedsysConfiguration configuration) {
        this.apiClient = apiClient;
        this.persistentStorage = persistentStorage;
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
        persistentStorage.put(StorageKeys.OAUTH_TOKEN, accessToken);
    }

    private String generateCodeVerifier() {
        return RandomUtils.generateRandomBase64UrlEncoded(48);
    }
}
