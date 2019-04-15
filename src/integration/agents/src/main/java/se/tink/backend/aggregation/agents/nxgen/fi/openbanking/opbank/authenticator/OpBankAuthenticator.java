package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.authenticator;

import java.util.Optional;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.OpBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.OpBankConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.OpBankConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.authenticator.rpc.AuthorizationResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.configuration.OpBankConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Authenticator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class OpBankAuthenticator implements OAuth2Authenticator {

    private final OpBankApiClient apiClient;
    private final PersistentStorage persistentStorage;
    private final OpBankConfiguration configuration;

    public OpBankAuthenticator(
            OpBankApiClient apiClient,
            PersistentStorage persistentStorage,
            OpBankConfiguration configuration) {
        this.apiClient = apiClient;
        this.persistentStorage = persistentStorage;
        this.configuration = configuration;
    }

    private OpBankConfiguration getConfiguration() {
        return Optional.ofNullable(configuration)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_CONFIGURATION));
    }

    @Override
    public URL buildAuthorizeUrl(String state) {
        TokenResponse newToken = this.apiClient.fetchNewToken();

        AuthorizationResponse authorization = this.apiClient.createNewAuthorization(newToken.getAccessToken());

        return null;
    }

    @Override
    public OAuth2Token exchangeAuthorizationCode(String code) {
        return null;
    }

    @Override
    public OAuth2Token refreshAccessToken(String refreshToken) throws SessionException {
        return null;
    }

    @Override
    public void useAccessToken(OAuth2Token accessToken) {
        persistentStorage.put(StorageKeys.OAUTH_TOKEN, accessToken);
    }
}
