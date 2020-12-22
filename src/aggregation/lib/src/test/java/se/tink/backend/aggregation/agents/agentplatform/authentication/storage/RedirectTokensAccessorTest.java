package se.tink.backend.aggregation.agents.agentplatform.authentication.storage;

import static org.junit.Assert.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationPersistedData;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.redirect.AgentRedirectTokensAuthenticationPersistedData;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.redirect.AgentRedirectTokensAuthenticationPersistedDataAccessorFactory;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.redirect.RedirectTokens;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.redirect.Token;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class RedirectTokensAccessorTest {

    private PersistentStorage persistentStorage;
    private AgentRedirectTokensAuthenticationPersistedData
            redirectTokensAuthenticationPersistedData;
    private ObjectMapper objectMapper;

    @Before
    public void init() {
        objectMapper = new ObjectMapper();
        persistentStorage = new PersistentStorage();
        AgentAuthenticationPersistedData agentAuthenticationPersistedData =
                new AgentAuthenticationPersistedData(persistentStorage);
        redirectTokensAuthenticationPersistedData =
                new AgentRedirectTokensAuthenticationPersistedDataAccessorFactory(objectMapper)
                        .createAgentRedirectTokensAuthenticationPersistedData(
                                agentAuthenticationPersistedData);
    }

    @Test
    public void redirectTokensToOAuth2TokenMapTest() {
        // given
        RedirectTokens redirectTokens =
                RedirectTokens.builder()
                        .accessToken(
                                Token.builder()
                                        .tokenType("dummyTokenType")
                                        .body("dummyTokenBody")
                                        .expiresIn(600L, 0L)
                                        .build())
                        .build();
        redirectTokensAuthenticationPersistedData.storeRedirectTokens(redirectTokens);
        RedirectTokensAccessor objectUnderTest =
                new RedirectTokensAccessor(persistentStorage, objectMapper);
        // when
        OAuth2Token result = objectUnderTest.getAccessToken();
        // then
        Assertions.assertThat(result.getAccessToken())
                .isEqualTo(
                        new String(
                                redirectTokens.getAccessToken().getBody(), StandardCharsets.UTF_8));
        Assertions.assertThat(result.getIssuedAt())
                .isEqualTo(redirectTokens.getAccessToken().getIssuedAtInSeconds());
        Assertions.assertThat(result.getExpiresInSeconds())
                .isEqualTo(redirectTokens.getAccessToken().getExpiresInSeconds());
        Assertions.assertThat(result.getTokenType())
                .isEqualTo(redirectTokens.getAccessToken().getTokenType());
    }
}
