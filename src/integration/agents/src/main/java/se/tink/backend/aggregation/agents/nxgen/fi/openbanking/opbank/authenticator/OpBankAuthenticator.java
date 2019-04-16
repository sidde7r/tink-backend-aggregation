package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.authenticator;

import com.google.common.collect.ImmutableList;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import org.apache.commons.codec.binary.Base64;
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
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Authenticator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class OpBankAuthenticator implements OAuth2Authenticator {

    private final OpBankApiClient apiClient;
    private final PersistentStorage persistentStorage;
    private final OpBankConfiguration configuration;

    public OpBankAuthenticator(
            OpBankApiClient apiClient,
            PersistentStorage persistentStorage,
            OpBankConfiguration configuration) {
        this.apiClient = apiClient;
        this.persistentStorage = persistentStorage;
        this.configuration = configuration;
    }

    private OpBankConfiguration getConfiguration() {
        return Optional.ofNullable(configuration)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_CONFIGURATION));
    }

    @Override
    public URL buildAuthorizeUrl(String state) {
        TokenResponse newToken = this.apiClient.fetchNewToken();

        AuthorizationResponse authorization =
                this.apiClient.createNewAuthorization(newToken.getAccessToken());

        ClaimsEntity claims =
                new ClaimsEntity(
                        new ClaimEntity(
                                new AuthorizationIdEntity(authorization.getAuthorizationId(), true),
                                null),
                        new ClaimEntity(
                                new AuthorizationIdEntity(authorization.getAuthorizationId(), true),
                                new AcrEntity(
                                        true,
                                        ImmutableList.of(
                                                "urn:openbanking:psd2:sca",
                                                "urn:openbanking:psd2:ca"))));

        TokenBodyEntity tokenBody = new TokenBodyEntity();
        tokenBody.setAud("https://mtls.apis.op.fi");
        tokenBody.setIss(configuration.getClientId());
        tokenBody.setResponse_type("code id_token");
        tokenBody.setClient_id(configuration.getClientId());
        tokenBody.setRedirect_uri("https://localhost:7357/api/v1/thirdparty/callback");
        tokenBody.setScope("openid accounts");
        tokenBody.setState(UUID.randomUUID().toString());
        tokenBody.setNonce(UUID.randomUUID().toString());
        tokenBody.setMax_age(86400);
        tokenBody.setIat(OffsetDateTime.now().toEpochSecond());
        tokenBody.setExp(OffsetDateTime.now().plusHours(1).toEpochSecond());
        tokenBody.setClaims(claims);

        String tokenBodyJson = SerializationUtils.serializeToString(tokenBody);
        String tokenHeadJson = SerializationUtils.serializeToString(new TokenHeaderEntity());


        String baseTokenString = Base64.encodeBase64URLSafeString(tokenHeadJson.getBytes())
            + "."
            + Base64.encodeBase64URLSafeString(tokenBodyJson.getBytes());

        String signature = apiClient.fetchSignature(baseTokenString);

        String fullToken = baseTokenString + "." + signature;

        String authorizationURL = String.format("https://authorize.psd2-sandbox.op.fi/oauth/authorize"
            + "?request=%s"
            + "&response_type=code id_token"
            + "&client_id=%s"
            + "&scope=openid accounts",
            fullToken,
            configuration.getClientId()).replace(" ", "%20");

        return new URL(authorizationURL);
    }

    @Override
    public OAuth2Token exchangeAuthorizationCode(String code) {
        return null;
    }

    @Override
    public OAuth2Token refreshAccessToken(String refreshToken) throws SessionException {
        return null;
    }

    @Override
    public void useAccessToken(OAuth2Token accessToken) {
        persistentStorage.put(StorageKeys.OAUTH_TOKEN, accessToken);
    }
}
