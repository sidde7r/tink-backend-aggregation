package se.tink.backend.aggregation.agents.agentplatform.authentication.storage;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationPersistedData;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.redirect.AgentRefreshableAccessTokenAuthenticationPersistedDataAccessorFactory;
import se.tink.backend.aggregation.agentsplatform.agentsframework.common.authentication.RefreshableAccessToken;
import se.tink.backend.aggregation.agentsplatform.agentsframework.common.authentication.Token;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.constants.OAuth2Constants;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

@AllArgsConstructor
public class OAuth2StorageMigrator implements AgentPlatformStorageMigrator {

    protected ObjectMapper objectMapper;

    public AgentAuthenticationPersistedData migrate(PersistentStorage persistentStorage) {
        AgentAuthenticationPersistedData agentAuthenticationPersistedData =
                new AgentAuthenticationPersistedData(new HashMap<>());
        if (!persistentStorage.containsKey(OAuth2Constants.PersistentStorageKeys.OAUTH_2_TOKEN)) {
            return agentAuthenticationPersistedData;
        }
        OAuth2Token oAuth2Token =
                persistentStorage
                        .get(OAuth2Constants.PersistentStorageKeys.OAUTH_2_TOKEN, OAuth2Token.class)
                        .get();
        RefreshableAccessToken.RefreshableAccessTokenBuilder redirectTokensBuilder =
                RefreshableAccessToken.builder().accessToken(migrateAccessToken(oAuth2Token));
        oAuth2Token
                .getRefreshToken()
                .map(body -> Token.builder().body(body).build())
                .ifPresent(refreshToken -> redirectTokensBuilder.refreshToken(refreshToken));
        return new AgentRefreshableAccessTokenAuthenticationPersistedDataAccessorFactory(
                        objectMapper)
                .createAgentRefreshableAccessTokenAuthenticationPersistedData(
                        agentAuthenticationPersistedData)
                .storeRefreshableAccessToken(redirectTokensBuilder.build());
    }

    private Token migrateAccessToken(OAuth2Token oAuth2Token) {
        return Token.builder()
                .tokenType(oAuth2Token.getTokenType())
                .body(oAuth2Token.getAccessToken())
                .expiresIn(oAuth2Token.getIssuedAt(), oAuth2Token.getExpiresInSeconds())
                .build();
    }
}
