package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.executor.payment;

import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersConstants.FormValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.authenticator.rpc.TokenForm;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.configuration.Xs2aDevelopersProviderConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Authenticator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class Xs2aDevelopersPaymentAuthenticator implements OAuth2Authenticator {
    private final Xs2aDevelopersApiClient apiClient;
    private final PersistentStorage persistentStorage;
    private final Xs2aDevelopersProviderConfiguration configuration;

    public Xs2aDevelopersPaymentAuthenticator(
            Xs2aDevelopersApiClient apiClient,
            PersistentStorage persistentStorage,
            Xs2aDevelopersProviderConfiguration configuration) {
        this.apiClient = apiClient;
        this.persistentStorage = persistentStorage;
        this.configuration = configuration;
    }

    @Override
    public URL buildAuthorizeUrl(String state) {
        return apiClient.buildAuthorizeUrl(
                state,
                "PIS:" + persistentStorage.get(StorageKeys.PAYMENT_ID),
                persistentStorage.get(StorageKeys.AUTHORISATION_URL));
    }

    @Override
    public OAuth2Token exchangeAuthorizationCode(String code) throws BankServiceException {
        TokenForm tokenForm =
                TokenForm.builder()
                        .setClientId(configuration.getClientId())
                        .setCode(code)
                        .setCodeVerifier(persistentStorage.get(StorageKeys.CODE_VERIFIER))
                        .setGrantType(FormValues.AUTHORIZATION_CODE)
                        .setRedirectUri(configuration.getRedirectUrl())
                        .setValidRequest(true)
                        .build();

        return apiClient.getToken(tokenForm).toTinkToken();
    }

    @Override
    public OAuth2Token refreshAccessToken(String refreshToken)
            throws SessionException, BankServiceException {
        TokenForm refreshTokenForm =
                TokenForm.builder()
                        .setClientId(configuration.getClientId())
                        .setGrantType(FormValues.REFRESH_TOKEN)
                        .setRefreshToken(refreshToken)
                        .build();

        return apiClient.getToken(refreshTokenForm).toTinkToken();
    }

    @Override
    public void useAccessToken(OAuth2Token accessToken) {
        persistentStorage.put(StorageKeys.PIS_TOKEN, accessToken);
    }
}
