package se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.authenticator;

import java.util.List;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.BelfiusApiClient;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.BelfiusConstants;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.BelfiusConstants.FormKeys;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.BelfiusConstants.FormValues;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.BelfiusConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.BelfiusConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.BelfiusConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.configuration.BelfiusConfiguration;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.utils.CryptoUtils;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Authenticator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.form.Form;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class BelfiusAuthenticator implements OAuth2Authenticator {

    private final BelfiusApiClient apiClient;
    private final PersistentStorage persistentStorage;
    private final BelfiusConfiguration configuration;
    private final String redirectUrl;
    private final String iban;

    public BelfiusAuthenticator(
            BelfiusApiClient apiClient,
            PersistentStorage persistentStorage,
            AgentConfiguration<BelfiusConfiguration> agentConfiguration,
            String iban) {
        this.apiClient = apiClient;
        this.persistentStorage = persistentStorage;
        this.configuration = agentConfiguration.getProviderSpecificConfiguration();
        this.redirectUrl = agentConfiguration.getRedirectUrl();
        this.iban = iban;
    }

    @Override
    public URL buildAuthorizeUrl(String state) {

        final String code = CryptoUtils.getCodeVerifier();
        persistentStorage.put(StorageKeys.CODE, code);
        try {
            List<ConsentResponse> consentResponseList =
                    apiClient.getConsent(
                            new URL(configuration.getBaseUrl() + Urls.CONSENT_PATH), iban, code);
            return new URL(
                    consentResponseList.get(0).getConsentUri()
                            + "&"
                            + Form.builder().put(QueryKeys.STATE, state).build());
        } catch (HttpResponseException ex) {
            if (ErrorDiscover.isChannelNotPermitted(ex)) {
                throw LoginError.NOT_SUPPORTED.exception(
                        "This account can't be consulted via electronic channel");
            }
            throw ex;
        }
    }

    @Override
    public OAuth2Token exchangeAuthorizationCode(String code) throws BankServiceException {

        String tokenEntity =
                Form.builder()
                        .put(FormKeys.GRANT_TYPE, FormValues.AUTHORIZATION_CODE)
                        .put(FormKeys.CODE, code)
                        .put(FormKeys.REDIRECT_URI, redirectUrl)
                        .put(FormKeys.CODE_VERIFIER, persistentStorage.get(StorageKeys.CODE))
                        .build()
                        .serialize();

        TokenResponse tokenResponse =
                apiClient.postToken(
                        new URL(configuration.getBaseUrl() + Urls.TOKEN_PATH), tokenEntity);
        persistentStorage.put(StorageKeys.ID_TOKEN, tokenResponse.getIdToken());
        persistentStorage.put(StorageKeys.LOGICAL_ID, tokenResponse.getLogicalId());
        persistentStorage.put(StorageKeys.SCA_TOKEN, tokenResponse.getScaToken());
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

        TokenResponse tokenResponse = new TokenResponse();
        try {
            tokenResponse =
                    apiClient.postToken(
                            new URL(configuration.getBaseUrl() + Urls.TOKEN_PATH),
                            refreshTokenEntity);
        } catch (HttpResponseException h) {
            if (h.getResponse().getStatus()
                    == BelfiusConstants.ErrorMessages.INTERNAL_SERVER_ERROR) {
                throw SessionError.SESSION_EXPIRED.exception();
            }
        }
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
