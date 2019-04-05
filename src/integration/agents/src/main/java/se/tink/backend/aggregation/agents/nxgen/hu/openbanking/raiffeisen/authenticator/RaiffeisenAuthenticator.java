package se.tink.backend.aggregation.agents.nxgen.hu.openbanking.raiffeisen.authenticator;

import java.util.Collections;
import java.util.List;
import se.tink.backend.aggregation.agents.exceptions.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.nxgen.hu.openbanking.raiffeisen.RaiffeisenApiClient;
import se.tink.backend.aggregation.agents.nxgen.hu.openbanking.raiffeisen.RaiffeisenConstants;
import se.tink.backend.aggregation.agents.nxgen.hu.openbanking.raiffeisen.authenticator.entities.AccessEntity;
import se.tink.backend.aggregation.agents.nxgen.hu.openbanking.raiffeisen.authenticator.entities.AccountInfoEntity;
import se.tink.backend.aggregation.agents.nxgen.hu.openbanking.raiffeisen.authenticator.rpc.GetConsentRequest;
import se.tink.backend.aggregation.agents.nxgen.hu.openbanking.raiffeisen.authenticator.rpc.GetConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.hu.openbanking.raiffeisen.authenticator.rpc.GetTokenForm;
import se.tink.backend.aggregation.agents.nxgen.hu.openbanking.raiffeisen.authenticator.rpc.RefreshTokenForm;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Authenticator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class RaiffeisenAuthenticator implements OAuth2Authenticator {
    private final RaiffeisenApiClient apiClient;
    private final SessionStorage sessionStorage;

    public RaiffeisenAuthenticator(
            RaiffeisenApiClient apiClient,
            SessionStorage sessionStorage,
            PersistentStorage persistentStorage) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public URL buildAuthorizeUrl(String state) {
        List<AccountInfoEntity> accountInfoEntityList =
                Collections.singletonList(
                        new AccountInfoEntity(
                                apiClient.getConfiguration().getIban(),
                                RaiffeisenConstants.Market.CURRENCY));

        GetConsentRequest getConsentRequest =
                new GetConsentRequest(
                        new AccessEntity(
                                accountInfoEntityList,
                                accountInfoEntityList,
                                accountInfoEntityList),
                        RaiffeisenConstants.FormValues.TRUE,
                        RaiffeisenConstants.FormValues.VALID_UNTIL,
                        RaiffeisenConstants.FormValues.FREQUENCY_PER_DAY,
                        RaiffeisenConstants.FormValues.FALSE);

        GetConsentResponse getConsentResponse = apiClient.getConsent(getConsentRequest);
        sessionStorage.put(
                RaiffeisenConstants.StorageKeys.CONSENT_ID, getConsentResponse.getConsentId());
        return apiClient.getUrl(state, getConsentResponse);
    }

    @Override
    public OAuth2Token exchangeAuthorizationCode(String code) throws BankServiceException {
        GetTokenForm getTokenForm =
                GetTokenForm.builder()
                        .setClientId(apiClient.getConfiguration().getClientId())
                        .setClientSecret(apiClient.getConfiguration().getClientSecret())
                        .setRedirectUri(apiClient.getConfiguration().getRedirectUri())
                        .setGrantType(RaiffeisenConstants.FormValues.AUTHORIZATION_CODE)
                        .setCode(code)
                        .build();

        return apiClient.getToken(getTokenForm);
    }

    @Override
    public OAuth2Token refreshAccessToken(String refreshToken)
            throws SessionException, BankServiceException {

        RefreshTokenForm refreshTokenForm =
                RefreshTokenForm.builder()
                        .setClientId(apiClient.getConfiguration().getClientId())
                        .setClientSecret(apiClient.getConfiguration().getClientSecret())
                        .setGrantType(RaiffeisenConstants.FormValues.REFRESH_TOKEN)
                        .setRefreshToken(refreshToken)
                        .build();

        return apiClient.refreshToken(refreshTokenForm);
    }

    @Override
    public void useAccessToken(OAuth2Token accessToken) {
        sessionStorage.put(RaiffeisenConstants.StorageKeys.TOKEN, accessToken);
    }
}
