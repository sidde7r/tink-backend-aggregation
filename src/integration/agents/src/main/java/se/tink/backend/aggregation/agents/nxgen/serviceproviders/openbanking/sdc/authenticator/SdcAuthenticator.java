package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sdc.authenticator;

import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sdc.SdcApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sdc.SdcConstants.StorageKeys;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

@RequiredArgsConstructor
public class SdcAuthenticator {

    private final SdcApiClient apiClient;
    private final PersistentStorage persistentStorage;

    public URL buildAuthorizeUrl(String state) {
        return apiClient.buildAuthorizeUrl(state);
    }

    public OAuth2Token exchangeAuthorizationCode(String code) {
        return apiClient.exchangeAuthorizationCode(code);
    }

    public void useAccessToken(OAuth2Token accessToken) {
        persistentStorage.put(StorageKeys.OAUTH_TOKEN, accessToken);
    }

    public void refreshAccessToken() {
        apiClient.refreshAccessToken();
    }
}
