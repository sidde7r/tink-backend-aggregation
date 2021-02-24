package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.authenticator;

import static se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.OpBankConstants.AuthorizationKeys;
import static se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.OpBankConstants.AuthorizationValues;
import static se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.OpBankConstants.RefreshTokenFormKeys;
import static se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.OpBankConstants.TokenValues;
import static se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.OpBankConstants.Urls;

import com.google.common.collect.ImmutableList;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;
import lombok.SneakyThrows;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.OpBankApiClient;
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
import se.tink.backend.aggregation.configuration.agents.utils.CertificateUtils;
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
    private String organizationIdentifier;

    @SneakyThrows
    public OpBankAuthenticator(
            OpBankApiClient apiClient,
            PersistentStorage persistentStorage,
            Credentials credentials,
            AgentConfiguration<OpBankConfiguration> agentConfiguration) {
        this.apiClient = apiClient;
        this.persistentStorage = persistentStorage;
        this.configuration = agentConfiguration.getProviderSpecificConfiguration();
        this.redirectUrl = agentConfiguration.getRedirectUrl();
        this.credentials = credentials;
        this.organizationIdentifier =
                CertificateUtils.getOrganizationIdentifier(agentConfiguration.getQsealc());
    }

    private String getRedirectUrl() {
        return Optional.ofNullable(redirectUrl)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_CONFIGURATION));
    }

    @SneakyThrows
    @Override
    public URL buildAuthorizeUrl(String state) {
        TokenResponse newToken = this.apiClient.fetchNewToken();
        AuthorizationResponse authorization =
                this.apiClient.createNewAuthorization(newToken.getAccessToken());

        credentials.setSessionExpiryDate(
                OpenBankingTokenExpirationDateHelper.getExpirationDateFrom(
                        newToken.toTinkToken(),
                        RefreshTokenFormKeys.DEFAULT_TOKEN_LIFETIME,
                        RefreshTokenFormKeys.DEFAULT_TOKEN_LIFETIME_UNIT));
        persistentStorage.put(PersistentStorageKeys.OAUTH_2_TOKEN, newToken.toTinkToken());
        // Tell the authenticator which access token it can use.
        useAccessToken(newToken.toTinkToken());

        TokenBodyEntity tokenBody = buildTokenBodyEntity(authorization, state);
        return buildAuthorizationURL(tokenBody);
    }

    private TokenBodyEntity buildTokenBodyEntity(
            AuthorizationResponse authorization, String state) {
        ClaimsEntity claims =
                new ClaimsEntity(
                        new ClaimEntity(
                                new AuthorizationIdEntity(authorization.getAuthorizationId(), true),
                                null),
                        new ClaimEntity(
                                new AuthorizationIdEntity(authorization.getAuthorizationId(), true),
                                new AcrEntity(true, ImmutableList.of("urn:openbanking:psd2:sca"))));

        return TokenBodyEntity.builder()
                .aud(Urls.BASE_URL)
                .iss(configuration.getClientId())
                .response_type(TokenValues.RESPONSE_TYPE)
                .client_id(configuration.getClientId())
                .redirect_uri(getRedirectUrl())
                .scope(TokenValues.SCOPE)
                .state(state)
                .nonce(UUID.randomUUID().toString())
                .max_age(TokenValues.MAX_AGE)
                .iat(OffsetDateTime.now().toEpochSecond())
                .exp(OffsetDateTime.now().plusHours(1).toEpochSecond())
                .claims(claims)
                .build();
    }

    @SneakyThrows
    private URL buildAuthorizationURL(TokenBodyEntity tokenBody) {
        String tokenBodyJson = SerializationUtils.serializeToString(tokenBody);
        String tokenHeadJson =
                SerializationUtils.serializeToString(new TokenHeaderEntity(organizationIdentifier));

        String baseTokenString =
                String.format(
                        "%s.%s",
                        Base64.getUrlEncoder()
                                .encodeToString(tokenHeadJson.getBytes())
                                .replace("=", ""),
                        Base64.getUrlEncoder()
                                .encodeToString(tokenBodyJson.getBytes())
                                .replace("=", ""));
        String signature = apiClient.fetchSignature(baseTokenString);
        String fullToken = String.format("%s.%s", baseTokenString, signature).replace("=", "");
        return new URL(Urls.AUTHORIZATION_URL)
                .queryParam(AuthorizationKeys.REQUEST, fullToken)
                .queryParam(AuthorizationKeys.RESPONSE_TYPE, AuthorizationValues.CODE)
                .queryParam(AuthorizationKeys.CLIENT_ID, configuration.getClientId())
                .queryParam(AuthorizationKeys.SCOPE, AuthorizationValues.OPENID_ACCOUNTS);
    }

    @Override
    public OAuth2Token exchangeAuthorizationCode(String code) {
        return apiClient.exchangeToken(code).toOauth2Token();
    }

    @Override
    public OAuth2Token refreshAccessToken(String refreshToken) throws SessionException {
        OAuth2Token newToken = this.apiClient.fetchRefreshToken(refreshToken);
        persistentStorage.put(RefreshTokenFormKeys.OAUTH2_ACCESS_TOKEN, newToken);
        return newToken;
    }

    @Override
    public void useAccessToken(OAuth2Token accessToken) {
        persistentStorage.put(StorageKeys.OAUTH_TOKEN, accessToken);
    }
}
