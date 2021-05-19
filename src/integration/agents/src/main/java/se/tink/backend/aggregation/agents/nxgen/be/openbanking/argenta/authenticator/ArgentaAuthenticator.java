package se.tink.backend.aggregation.agents.nxgen.be.openbanking.argenta.authenticator;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.argenta.ArgentaApiClient;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.argenta.ArgentaConstants.CredentialKeys;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.argenta.ArgentaConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.argenta.authenticator.entities.IbanEntity;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.argenta.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Authenticator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class ArgentaAuthenticator implements OAuth2Authenticator {

    private final Credentials credentials;
    private final ArgentaApiClient apiClient;
    private final PersistentStorage persistentStorage;

    public ArgentaAuthenticator(
            Credentials credentials,
            ArgentaApiClient apiClient,
            PersistentStorage persistentStorage) {
        this.credentials = credentials;
        this.apiClient = apiClient;
        this.persistentStorage = persistentStorage;
    }

    @Override
    public URL buildAuthorizeUrl(String state) {
        List<IbanEntity> ibans =
                Stream.of(credentials.getField(CredentialKeys.IBAN).split(","))
                        .map(String::trim)
                        .map(IbanEntity::new)
                        .collect(Collectors.toList());

        ConsentResponse consentResponse = apiClient.getConsent(ibans);

        persistentStorage.put(StorageKeys.CONSENT_ID, consentResponse.getConsentId());
        return apiClient.buildAuthorizeUrl(state, consentResponse.getConsentId());
    }

    @Override
    public OAuth2Token exchangeAuthorizationCode(String code) {
        return apiClient.exchangeAuthorizationCode(code);
    }

    @Override
    public OAuth2Token refreshAccessToken(String refreshToken) {
        return apiClient.exchangeRefreshToken(refreshToken);
    }

    @Override
    public void useAccessToken(OAuth2Token accessToken) {
        persistentStorage.put(StorageKeys.OAUTH_TOKEN, accessToken);
    }
}
