package se.tink.backend.aggregation.agents.agentplatform.authentication.storage;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationPersistedData;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.redirect.AgentRedirectTokensAuthenticationPersistedDataAccessorFactory;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.redirect.RedirectTokens;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.redirect.Token;
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
        RedirectTokens.RedirectTokensBuilder redirectTokensBuilder =
                RedirectTokens.builder().accessToken(migrateAccessToken(oAuth2Token));
        oAuth2Token
                .getRefreshToken()
                .map(body -> Token.builder().body(body).build())
                .ifPresent(refreshToken -> redirectTokensBuilder.refreshToken(refreshToken));
        return new AgentRedirectTokensAuthenticationPersistedDataAccessorFactory(objectMapper)
                .createAgentRedirectTokensAuthenticationPersistedData(
                        agentAuthenticationPersistedData)
                .storeRedirectTokens(redirectTokensBuilder.build());
    }

    private Token migrateAccessToken(OAuth2Token oAuth2Token) {
        return Token.builder()
                .tokenType(oAuth2Token.getTokenType())
                .body(oAuth2Token.getAccessToken())
                .expiresIn(
                        oAuth2Token.getIssuedAt() * 1000, oAuth2Token.getExpiresInSeconds() * 1000)
                .build();
    }
}
