package se.tink.backend.aggregation.agents.agentplatform.authentication.storage;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationPersistedData;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.redirect.AgentRedirectTokensAuthenticationPersistedData;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.redirect.AgentRedirectTokensAuthenticationPersistedDataAccessorFactory;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.redirect.RedirectTokens;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2TokenAccessor;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class RedirectTokensAccessor implements OAuth2TokenAccessor {

    private final PersistentStorage persistentStorage;
    private final AgentRedirectTokensAuthenticationPersistedData redirectTokensPersistedData;

    public RedirectTokensAccessor(PersistentStorage persistentStorage, ObjectMapper objectMapper) {
        this.persistentStorage = persistentStorage;
        redirectTokensPersistedData =
                new AgentRedirectTokensAuthenticationPersistedDataAccessorFactory(objectMapper)
                        .createAgentRedirectTokensAuthenticationPersistedData(
                                new AgentAuthenticationPersistedData(persistentStorage));
    }

    @Override
    public void invalidate() {
        persistentStorage.clear();
    }

    @Override
    public OAuth2Token getAccessToken() {
        return redirectTokensPersistedData
                .getRedirectTokens()
                .map(t -> mapToLegacyOAuth2Token(t))
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        "RedirectTokens can't be empty. Check if this method is called in correct state"));
    }

    private OAuth2Token mapToLegacyOAuth2Token(RedirectTokens redirectTokens) {
        return new OAuth2Token(
                redirectTokens.getAccessToken().getTokenType(),
                new String(redirectTokens.getAccessToken().getBody(), StandardCharsets.UTF_8),
                null,
                null,
                redirectTokens.getAccessToken().getExpiresInSeconds(),
                0,
                redirectTokens.getAccessToken().getIssuedAtInSeconds());
    }
}
