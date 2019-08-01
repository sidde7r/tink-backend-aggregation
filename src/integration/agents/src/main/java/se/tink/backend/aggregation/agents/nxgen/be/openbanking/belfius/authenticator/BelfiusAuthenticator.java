package se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.authenticator;

import java.util.List;
import se.tink.backend.aggregation.agents.exceptions.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.BelfiusApiClient;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.BelfiusConstants.FormKeys;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.BelfiusConstants.FormValues;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.BelfiusConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.BelfiusConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.BelfiusConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.configuration.BelfiusConfiguration;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.utils.CryptoUtils;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Authenticator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.Form;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class BelfiusAuthenticator implements OAuth2Authenticator {

    private final BelfiusApiClient apiClient;
    private final PersistentStorage persistentStorage;
    private final BelfiusConfiguration configuration;
    private final String iban;

    public BelfiusAuthenticator(
            BelfiusApiClient apiClient,
            PersistentStorage persistentStorage,
            BelfiusConfiguration configuration,
            String iban) {
        this.apiClient = apiClient;
        this.persistentStorage = persistentStorage;
        this.configuration = configuration;
        this.iban = iban;
    }

    @Override
    public URL buildAuthorizeUrl(String state) {

        final String code = CryptoUtils.getCodeVerifier();
        persistentStorage.put(StorageKeys.CODE, code);

        List<ConsentResponse> consentResponseList =
                apiClient.getConsent(
                        new URL(configuration.getBaseUrl() + Urls.CONSENT_PATH), iban, code);
        return new URL(
                consentResponseList.get(0).getConsentUri()
                        + "&"
                        + Form.builder().put(QueryKeys.STATE, state).build()
                        + state);
    }

    @Override
    public OAuth2Token exchangeAuthorizationCode(String code) throws BankServiceException {

        String tokenEntity =
                Form.builder()
                        .put(FormKeys.GRANT_TYPE, FormValues.AUTHORIZATION_CODE)
                        .put(FormKeys.CODE, code)
                        .put(FormKeys.REDIRECT_URI, configuration.getRedirectUrl())
                        .put(FormKeys.CODE_VERIFIER, persistentStorage.get(StorageKeys.CODE))
                        .build()
                        .serialize();

        TokenResponse tokenResponse =
                apiClient.postToken(
                        new URL(configuration.getBaseUrl() + Urls.TOKEN_PATH), tokenEntity);
        persistentStorage.put(StorageKeys.ID_TOKEN, tokenResponse.getIdToken());
        persistentStorage.put(StorageKeys.LOGICAL_ID, tokenResponse.getLogicalId());
        return OAuth2Token.create(
                tokenResponse.getTokenType(),
                tokenResponse.getAccessToken(),
                tokenResponse.getRefreshToken(),
                tokenResponse.getExpiresIn());
    }

    @Override
    public OAuth2Token refreshAccessToken(String refreshToken)
            throws SessionException, BankServiceException {

        String refreshTokenEntity =
                Form.builder()
                        .put(FormKeys.GRANT_TYPE, FormValues.REFRESH_TOKEN)
                        .put(FormKeys.REFRESH_TOKEN, refreshToken)
                        .build()
                        .serialize();

        TokenResponse tokenResponse =
                apiClient.postToken(
                        new URL(configuration.getBaseUrl() + Urls.TOKEN_PATH), refreshTokenEntity);
        persistentStorage.put(StorageKeys.ID_TOKEN, tokenResponse.getIdToken());
        persistentStorage.put(StorageKeys.LOGICAL_ID, tokenResponse.getLogicalId());
        return OAuth2Token.create(
                tokenResponse.getTokenType(),
                tokenResponse.getAccessToken(),
                tokenResponse.getRefreshToken(),
                tokenResponse.getExpiresIn());
    }

    @Override
    public void useAccessToken(OAuth2Token accessToken) {
        persistentStorage.put(StorageKeys.OAUTH_TOKEN, accessToken);
    }
}
