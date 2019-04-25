package se.tink.backend.aggregation.agents.nxgen.be.openbanking.crelan.authenticator;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.crelan.CrelanApiClient;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.crelan.CrelanConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.crelan.CrelanConstants.FormValues;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.crelan.CrelanConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.crelan.authenticator.entities.AccessEntity;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.crelan.authenticator.entities.AccessInfoEntity;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.crelan.authenticator.rpc.GetTokenForm;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.crelan.authenticator.rpc.PostConsentBody;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.crelan.authenticator.rpc.PostConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.crelan.configuration.CrelanConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Authenticator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class CrelanAuthenticator implements OAuth2Authenticator {

    private final CrelanApiClient apiClient;
    private final PersistentStorage persistentStorage;
    private final CrelanConfiguration configuration;
    private final String iban;

    public CrelanAuthenticator(
            CrelanApiClient apiClient,
            PersistentStorage persistentStorage,
            CrelanConfiguration configuration,
            String iban) {
        this.apiClient = apiClient;
        this.persistentStorage = persistentStorage;
        this.configuration = configuration;
        this.iban = iban;
    }

    private CrelanConfiguration getConfiguration() {
        return Optional.ofNullable(configuration)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_CONFIGURATION));
    }

    @Override
    public URL buildAuthorizeUrl(String state) {
        List<AccessInfoEntity> accessInfoEntityList =
                Collections.singletonList(new AccessInfoEntity(FormValues.EUR, iban));
        AccessEntity accessEntity =
                new AccessEntity(accessInfoEntityList, accessInfoEntityList, accessInfoEntityList);
        PostConsentBody postConsentBody =
                new PostConsentBody(
                        accessEntity,
                        FormValues.FALSE,
                        FormValues.FREQUENCY_PER_DAY,
                        FormValues.TRUE,
                        FormValues.VALID_UNTIL);

        PostConsentResponse postConsentResponse = apiClient.createConsent(postConsentBody);
        persistentStorage.put(StorageKeys.CONSENT_ID, postConsentResponse.getConsentId());
        return apiClient.buildAuthorizeUrl(state);
    }

    @Override
    public OAuth2Token exchangeAuthorizationCode(String code) {
        GetTokenForm getTokenForm =
                GetTokenForm.builder()
                        .setClientId(configuration.getClientId())
                        .setCode(code)
                        .setCodeVerifier(FormValues.CODE_VERIFIER)
                        .setGrantType(FormValues.AUTHORIZATION_CODE)
                        .setRedirectUri(configuration.getRedirectUrl())
                        .setValidRequest(true)
                        .build();

        return apiClient.getToken(getTokenForm).toTinkToken();
    }

    @Override
    public OAuth2Token refreshAccessToken(String refreshToken) throws SessionException {
        GetTokenForm refreshTokenForm =
                GetTokenForm.builder()
                        .setClientId(configuration.getClientId())
                        .setGrantType(FormValues.REFRESH_TOKEN)
                        .setRefreshToken(refreshToken)
                        .build();

        return apiClient.getToken(refreshTokenForm).toTinkToken();
    }

    @Override
    public void useAccessToken(OAuth2Token accessToken) {
        persistentStorage.put(StorageKeys.OAUTH_TOKEN, accessToken);
    }
}
