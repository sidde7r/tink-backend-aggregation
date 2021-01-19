package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling;

import java.util.HashMap;
import se.tink.backend.aggregation.agents.agentplatform.authentication.ObjectMapperFactory;
import se.tink.backend.aggregation.agents.agentplatform.authentication.storage.AgentPlatformStorageMigrator;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationPersistedData;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.redirect.AgentRefreshableAccessTokenAuthenticationPersistedData;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.redirect.AgentRefreshableAccessTokenAuthenticationPersistedDataAccessorFactory;
import se.tink.backend.aggregation.agentsplatform.agentsframework.common.authentication.RefreshableAccessToken;
import se.tink.backend.aggregation.agentsplatform.agentsframework.common.authentication.Token;
import se.tink.backend.aggregation.nxgen.controllers.authentication.oauth.OAuth2Token;
import se.tink.backend.aggregation.nxgen.controllers.authentication.oauth.OAuth2TokenStorageDefaultImpl;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class StarlingAgentPlatformStorageMigrator implements AgentPlatformStorageMigrator {

    @Override
    public AgentAuthenticationPersistedData migrate(PersistentStorage ps) {
        AgentRefreshableAccessTokenAuthenticationPersistedData
                redirectTokensAuthenticationPersistedData =
                        new AgentRefreshableAccessTokenAuthenticationPersistedDataAccessorFactory(
                                        new ObjectMapperFactory().getInstance())
                                .createAgentRefreshableAccessTokenAuthenticationPersistedData(
                                        new AgentAuthenticationPersistedData(ps));

        return new OAuth2TokenStorageDefaultImpl(ps)
                .fetchToken()
                .map(oauth2Token -> mapOAuth2TokenToRedirectTokens(oauth2Token))
                .map(
                        redirectTokens ->
                                redirectTokensAuthenticationPersistedData
                                        .storeRefreshableAccessToken(redirectTokens))
                .orElse(new AgentAuthenticationPersistedData(new HashMap<>()));
    }

    private RefreshableAccessToken mapOAuth2TokenToRedirectTokens(OAuth2Token oAuth2Token) {
        RefreshableAccessToken.RefreshableAccessTokenBuilder builder =
                RefreshableAccessToken.builder()
                        .accessToken(
                                Token.builder()
                                        .body(oAuth2Token.getAccessToken())
                                        .tokenType(oAuth2Token.getTokenType())
                                        .expiresIn(
                                                oAuth2Token.getIssuedAt(),
                                                oAuth2Token.getExpiresIn())
                                        .build());
        if (oAuth2Token.canRefresh()) {
            builder.refreshToken(Token.builder().body(oAuth2Token.getRefreshToken()).build());
        }
        return builder.build();
    }
}
