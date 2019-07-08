package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.paypal.authenticator;

import java.util.Optional;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.paypal.PayPalApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.paypal.PayPalConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.paypal.PayPalConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.paypal.configuration.PayPalConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Authenticator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class PayPalAuthenticator implements OAuth2Authenticator {

    private final PayPalApiClient apiClient;
    private final PersistentStorage persistentStorage;
    private final PayPalConfiguration configuration;

    public PayPalAuthenticator(
            PayPalApiClient apiClient,
            PersistentStorage persistentStorage,
            PayPalConfiguration configuration) {
        this.apiClient = apiClient;
        this.persistentStorage = persistentStorage;
        this.configuration = configuration;
    }

    private PayPalConfiguration getConfiguration() {
        return Optional.ofNullable(configuration)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_CONFIGURATION));
    }

    @Override
    public URL buildAuthorizeUrl(String state) {
        return apiClient.getAuthorizeUrl(state);
    }

    @Override
    public OAuth2Token exchangeAuthorizationCode(String code) {
        return apiClient.getToken(code);
    }

    @Override
    public OAuth2Token refreshAccessToken(String refreshToken) throws SessionException {
        OAuth2Token token = apiClient.refreshToken(refreshToken);
        persistentStorage.put(StorageKeys.OAUTH_TOKEN, token);
        return token;
    }

    @Override
    public void useAccessToken(OAuth2Token accessToken) {
        persistentStorage.put(StorageKeys.OAUTH_TOKEN, accessToken);
    }
}
