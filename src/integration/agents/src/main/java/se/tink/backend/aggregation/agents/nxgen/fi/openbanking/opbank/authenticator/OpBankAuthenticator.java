package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.authenticator;

import com.google.common.collect.ImmutableList;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.OpBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.OpBankConstants;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.OpBankConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.OpBankConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.authenticator.entities.AcrEntity;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.authenticator.entities.AuthorizationIdEntity;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.authenticator.entities.ClaimEntity;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.authenticator.entities.ClaimsEntity;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.authenticator.entities.TokenBodyEntity;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.authenticator.entities.TokenHeaderEntity;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.authenticator.rpc.AuthorizationResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.configuration.OpBankConfiguration;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.constants.OAuth2Constants.PersistentStorageKeys;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.OpenBankingTokenExpirationDateHelper;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class OpBankAuthenticator implements OAuth2Authenticator {

    private final OpBankApiClient apiClient;
    private final PersistentStorage persistentStorage;
    private final OpBankConfiguration configuration;
    private final String redirectUrl;
    private final Credentials credentials;
    private String refreshToken;

    public OpBankAuthenticator(
            OpBankApiClient apiClient,
            PersistentStorage persistentStorage,
            Credentials credentials,
            AgentConfiguration<OpBankConfiguration> agentConfiguration) {
        this.apiClient = apiClient;
        this.persistentStorage = persistentStorage;
        this.configuration = agentConfiguration.getClientConfiguration();
        this.redirectUrl = agentConfiguration.getRedirectUrl();
        this.credentials = credentials;
    }

    private OpBankConfiguration getConfiguration() {
        return Optional.ofNullable(configuration)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_CONFIGURATION));
    }

    private String getRedirectUrl() {
        return Optional.ofNullable(redirectUrl)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_CONFIGURATION));
    }

    @Override
    public URL buildAuthorizeUrl(String state) {
        TokenResponse newToken = this.apiClient.fetchNewToken();
        AuthorizationResponse authorization =
                this.apiClient.createNewAuthorization(newToken.getAccessToken());

        refreshToken = newToken.getRefreshToken();

        credentials.setSessionExpiryDate(
                OpenBankingTokenExpirationDateHelper.getExpirationDateFrom(
                        newToken.toTinkToken(),
                        OpBankConstants.RefreshTokenFormKeys.DEFAULT_TOKEN_LIFETIME,
                        OpBankConstants.RefreshTokenFormKeys.DEFAULT_TOKEN_LIFETIME_UNIT));
        persistentStorage.put(PersistentStorageKeys.OAUTH_2_TOKEN, newToken.toTinkToken());
        // Tell the authenticator which access token it can use.
        useAccessToken(newToken.toTinkToken());

        ClaimsEntity claims =
                new ClaimsEntity(
                        new ClaimEntity(
                                new AuthorizationIdEntity(authorization.getAuthorizationId(), true),
                                null),
                        new ClaimEntity(
                                new AuthorizationIdEntity(authorization.getAuthorizationId(), true),
                                new AcrEntity(true, ImmutableList.of("urn:openbanking:psd2:sca"))));

        TokenBodyEntity tokenBody = new TokenBodyEntity();
        tokenBody.setAud(OpBankConstants.Urls.BASE_URL);
        tokenBody.setIss(configuration.getClientId());
        tokenBody.setResponseType(OpBankConstants.TokenValues.RESPONSE_TYPE);
        tokenBody.setClientId(configuration.getClientId());
        tokenBody.setRedirectUri(getRedirectUrl());
        tokenBody.setScope(OpBankConstants.TokenValues.SCOPE);
        tokenBody.setState(state);
        tokenBody.setNonce(UUID.randomUUID().toString());
        tokenBody.setMaxAge(OpBankConstants.TokenValues.MAX_AGE);
        tokenBody.setIat(OffsetDateTime.now().toEpochSecond());
        tokenBody.setExp(OffsetDateTime.now().plusHours(1).toEpochSecond());
        tokenBody.setClaims(claims);

        String tokenBodyJson = SerializationUtils.serializeToString(tokenBody);
        String tokenHeadJson = SerializationUtils.serializeToString(new TokenHeaderEntity());

        String baseTokenString =
                Base64.getUrlEncoder().encodeToString(tokenHeadJson.getBytes()).replaceAll("=", "")
                        + "."
                        + Base64.getUrlEncoder()
                                .encodeToString(tokenBodyJson.getBytes())
                                .replaceAll("=", "");
        String signature = apiClient.fetchSignature(baseTokenString);
        String fullToken = baseTokenString + "." + signature;
        fullToken = fullToken.replaceAll("=", "");
        URL authorizationURL =
                new URL(OpBankConstants.Urls.AUTHORIZATION_URL)
                        .queryParam(OpBankConstants.AuthorizationKeys.REQUEST, fullToken)
                        .queryParam(
                                OpBankConstants.AuthorizationKeys.RESPONSE_TYPE,
                                OpBankConstants.AuthorizationValues.CODE)
                        .queryParam(
                                OpBankConstants.AuthorizationKeys.CLIENT_ID,
                                configuration.getClientId())
                        .queryParam(
                                OpBankConstants.AuthorizationKeys.SCOPE,
                                OpBankConstants.AuthorizationValues.OPENID_ACCOUNTS);

        return authorizationURL;
    }

    @Override
    public OAuth2Token exchangeAuthorizationCode(String code) {
        return apiClient.exchangeToken(code).toOauth2Token();
    }

    @Override
    public OAuth2Token refreshAccessToken(String refreshToken) throws SessionException {
        OAuth2Token newToken = this.apiClient.fetchRefreshToken(refreshToken);
        persistentStorage.put(OpBankConstants.RefreshTokenFormKeys.OAUTH2_ACCESS_TOKEN, newToken);
        return newToken;
    }

    @Override
    public void useAccessToken(OAuth2Token accessToken) {
        persistentStorage.put(StorageKeys.OAUTH_TOKEN, accessToken);
    }
}
