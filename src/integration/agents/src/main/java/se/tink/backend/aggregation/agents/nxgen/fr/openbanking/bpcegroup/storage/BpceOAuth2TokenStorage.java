package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.storage;

import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.constants.OAuth2Constants;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

@RequiredArgsConstructor
public class BpceOAuth2TokenStorage {

    private final PersistentStorage persistentStorage;

    public void storeToken(OAuth2Token accessToken) {
        persistentStorage.put(OAuth2Constants.PersistentStorageKeys.OAUTH_2_TOKEN, accessToken);
    }

    public OAuth2Token getToken() {
        return persistentStorage
                .get(OAuth2Constants.PersistentStorageKeys.OAUTH_2_TOKEN, OAuth2Token.class)
                .orElseThrow(() -> new IllegalStateException("Cannot find token."));
    }
}
