package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.authenticator;

import java.time.format.DateTimeFormatter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersConstants.FormValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.authenticator.entities.AccessEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.authenticator.rpc.GetTokenForm;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.authenticator.rpc.PostConsentBody;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.authenticator.rpc.PostConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.configuration.Xs2aDevelopersProviderConfiguration;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Authenticator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class Xs2aDevelopersAuthenticator implements OAuth2Authenticator {

    private final Xs2aDevelopersApiClient apiClient;
    private final PersistentStorage persistentStorage;
    private final Xs2aDevelopersProviderConfiguration configuration;
    private final LocalDateTimeSource localDateTimeSource;

    public Xs2aDevelopersAuthenticator(
            Xs2aDevelopersApiClient apiClient,
            PersistentStorage persistentStorage,
            Xs2aDevelopersProviderConfiguration configuration,
            LocalDateTimeSource localDateTimeSource) {
        this.apiClient = apiClient;
        this.persistentStorage = persistentStorage;
        this.configuration = configuration;
        this.localDateTimeSource = localDateTimeSource;
    }

    @Override
    public URL buildAuthorizeUrl(String state) {
        AccessEntity accessEntity = getAccessEntity();
        PostConsentBody postConsentBody =
                new PostConsentBody(
                        accessEntity,
                        FormValues.FALSE,
                        FormValues.FREQUENCY_PER_DAY,
                        FormValues.TRUE,
                        localDateTimeSource.now().plusDays(89).format(DateTimeFormatter.ISO_DATE));

        PostConsentResponse postConsentResponse = apiClient.createConsent(postConsentBody);
        persistentStorage.put(StorageKeys.CONSENT_ID, postConsentResponse.getConsentId());
        return apiClient.buildAuthorizeUrl(
                state,
                QueryValues.SCOPE + postConsentResponse.getConsentId(),
                postConsentResponse.getLinks().getScaOAuth());
    }

    @Override
    public OAuth2Token exchangeAuthorizationCode(String code) {
        GetTokenForm getTokenForm =
                GetTokenForm.builder()
                        .setClientId(configuration.getClientId())
                        .setCode(code)
                        .setCodeVerifier(persistentStorage.get(StorageKeys.CODE_VERIFIER))
                        .setGrantType(FormValues.AUTHORIZATION_CODE)
                        .setRedirectUri(configuration.getRedirectUrl())
                        .setValidRequest(true)
                        .build();

        return apiClient.getToken(getTokenForm).toTinkToken();
    }

    @Override
    public OAuth2Token refreshAccessToken(String refreshToken) {
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

    public void invalidateToken() {
        persistentStorage.remove(StorageKeys.OAUTH_TOKEN);
        persistentStorage.remove(StorageKeys.CONSENT_ID);
    }

    protected AccessEntity getAccessEntity() {
        return new AccessEntity(FormValues.ALL_ACCOUNTS);
    }
}
