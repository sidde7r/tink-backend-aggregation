package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling;

import java.util.HashMap;
import se.tink.backend.aggregation.agents.agentplatform.authentication.ObjectMapperFactory;
import se.tink.backend.aggregation.agents.agentplatform.authentication.storage.AgentPlatformStorageMigrator;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationPersistedData;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.redirect.AgentRedirectTokensAuthenticationPersistedData;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.redirect.AgentRedirectTokensAuthenticationPersistedDataAccessorFactory;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.redirect.RedirectTokens;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.redirect.Token;
import se.tink.backend.aggregation.nxgen.controllers.authentication.oauth.OAuth2Token;
import se.tink.backend.aggregation.nxgen.controllers.authentication.oauth.OAuth2TokenStorageDefaultImpl;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class StarlingAgentPlatformStorageMigrator implements AgentPlatformStorageMigrator {

    @Override
    public AgentAuthenticationPersistedData migrate(PersistentStorage ps) {
        AgentRedirectTokensAuthenticationPersistedData redirectTokensAuthenticationPersistedData =
                new AgentRedirectTokensAuthenticationPersistedDataAccessorFactory(
                                new ObjectMapperFactory().getInstance())
                        .createAgentRedirectTokensAuthenticationPersistedData(
                                new AgentAuthenticationPersistedData(ps));

        return new OAuth2TokenStorageDefaultImpl(ps)
                .fetchToken()
                .map(oauth2Token -> mapOAuth2TokenToRedirectTokens(oauth2Token))
                .map(
                        redirectTokens ->
                                redirectTokensAuthenticationPersistedData.storeRedirectTokens(
                                        redirectTokens))
                .orElse(new AgentAuthenticationPersistedData(new HashMap<>()));
    }

    private RedirectTokens mapOAuth2TokenToRedirectTokens(OAuth2Token oAuth2Token) {
        RedirectTokens.RedirectTokensBuilder builder =
                RedirectTokens.builder()
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
