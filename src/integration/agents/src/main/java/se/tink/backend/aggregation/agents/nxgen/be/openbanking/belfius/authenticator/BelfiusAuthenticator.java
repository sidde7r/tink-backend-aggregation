package se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.authenticator;

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
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Authenticator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.Form;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class BelfiusAuthenticator implements OAuth2Authenticator {

    private final BelfiusApiClient apiClient;
    private final PersistentStorage persistentStorage;
    private final BelfiusConfiguration configuration;

    public BelfiusAuthenticator(
            BelfiusApiClient apiClient,
            PersistentStorage persistentStorage,
            BelfiusConfiguration configuration) {
        this.apiClient = apiClient;
        this.persistentStorage = persistentStorage;
        this.configuration = configuration;
    }

    @Override
    public URL buildAuthorizeUrl(String state) {
        ConsentResponse[] test =
                apiClient.getConsent(new URL(configuration.getBaseUrl() + Urls.CONSENT_PATH));
        return new URL(test[0].getConsentUri() + QueryKeys.STATE + state);
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
        return null;
    }

    @Override
    public void useAccessToken(OAuth2Token accessToken) {}
}
