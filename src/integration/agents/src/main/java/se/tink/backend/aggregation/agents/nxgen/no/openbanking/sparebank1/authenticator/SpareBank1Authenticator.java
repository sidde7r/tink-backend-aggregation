package se.tink.backend.aggregation.agents.nxgen.no.openbanking.sparebank1.authenticator;

import java.util.Optional;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.sparebank1.SpareBank1ApiClient;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.sparebank1.SpareBank1Constants;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.sparebank1.SpareBank1Constants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.sparebank1.SpareBank1Constants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.sparebank1.authenticator.rpc.GetTokenForm;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.sparebank1.configuration.SpareBank1Configuration;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Authenticator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

// TODO: OAuth flow with authorization code will be implemented for production
public class SpareBank1Authenticator implements OAuth2Authenticator, Authenticator {

    private final SpareBank1ApiClient client;
    private final PersistentStorage persistentStorage;
    private final SpareBank1Configuration configuration;

    public SpareBank1Authenticator(
            SpareBank1ApiClient client,
            PersistentStorage persistentStorage,
            SpareBank1Configuration configuration) {
        this.client = client;
        this.persistentStorage = persistentStorage;
        this.configuration = configuration;
    }

    public SpareBank1Configuration getConfiguration() {
        return Optional.ofNullable(configuration)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_CONFIGURATION));
    }

    @Override
    public URL buildAuthorizeUrl(String state) {
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

    @Override
    public void authenticate(Credentials credentials)
            throws AuthenticationException, AuthorizationException {
        final String clientId = getConfiguration().getClientId();
        final String clientSecret = getConfiguration().getClientSecret();

        final GetTokenForm form =
                GetTokenForm.builder()
                        .setGrantType(SpareBank1Constants.FormValues.GRANT_TYPE)
                        .setClientId(clientId)
                        .setClientSecret(clientSecret)
                        .build();

        persistentStorage.put(StorageKeys.OAUTH_TOKEN, client.getToken(form));
    }
}
