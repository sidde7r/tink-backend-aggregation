package se.tink.backend.aggregation.agents.agentplatform.authentication.storage;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationPersistedData;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.redirect.AgentRedirectTokensAuthenticationPersistedData;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.redirect.AgentRedirectTokensAuthenticationPersistedDataAccessorFactory;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.redirect.RedirectTokens;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class OAuth2StorageMigratorTest {

    private static final String OAUTH2_STORAGE_KEY = "oauth2_access_token";
    private OAuth2Token oAuth2Token;
    private PersistentStorage persistentStorage;
    private OAuth2StorageMigrator objectUnderTest;
    private ObjectMapper objectMapper = new ObjectMapper();
    private AgentRedirectTokensAuthenticationPersistedDataAccessorFactory
            redirectTokensAuthenticationPersistedDataAccessorFactory;

    @Before
    public void init() {
        this.oAuth2Token = Mockito.mock(OAuth2Token.class);
        this.persistentStorage = Mockito.mock(PersistentStorage.class);
        Mockito.when(persistentStorage.get(OAUTH2_STORAGE_KEY, OAuth2Token.class))
                .thenReturn(Optional.of(oAuth2Token));
        Mockito.when(persistentStorage.containsKey(OAUTH2_STORAGE_KEY)).thenReturn(true);
        redirectTokensAuthenticationPersistedDataAccessorFactory =
                new AgentRedirectTokensAuthenticationPersistedDataAccessorFactory(objectMapper);
        objectUnderTest = new OAuth2StorageMigrator(objectMapper);
    }

    @Test
    public void shouldMapOnlyAccessToken() {
        // given
        mockAccessToken();
        // when
        AgentAuthenticationPersistedData result = objectUnderTest.migrate(persistentStorage);
        // then
        AgentRedirectTokensAuthenticationPersistedData redirectTokensAuthenticationPersistedData =
                redirectTokensAuthenticationPersistedDataAccessorFactory
                        .createAgentRedirectTokensAuthenticationPersistedData(result);
        Assertions.assertThat(
                        redirectTokensAuthenticationPersistedData.getRedirectTokens().isPresent())
                .isTrue();
        RedirectTokens redirectTokens =
                redirectTokensAuthenticationPersistedData.getRedirectTokens().get();
        Assertions.assertThat(redirectTokens.getAccessToken().getBody())
                .isEqualTo(oAuth2Token.getAccessToken().getBytes());
        Assertions.assertThat(redirectTokens.getAccessToken().getExpiresInSeconds())
                .isEqualTo(oAuth2Token.getExpiresInSeconds());
        Assertions.assertThat(redirectTokens.getAccessToken().getIssuedAtInSeconds())
                .isEqualTo(oAuth2Token.getIssuedAt());
        Assertions.assertThat(redirectTokens.getRefreshToken()).isNull();
    }

    @Test
    public void shouldMapRefreshToken() {
        // given
        mockAccessToken();
        Mockito.when(oAuth2Token.getRefreshToken()).thenReturn(Optional.of("dummyRefreshToken"));
        // when
        AgentAuthenticationPersistedData result = objectUnderTest.migrate(persistentStorage);
        // then
        AgentRedirectTokensAuthenticationPersistedData redirectTokensAuthenticationPersistedData =
                redirectTokensAuthenticationPersistedDataAccessorFactory
                        .createAgentRedirectTokensAuthenticationPersistedData(result);
        Assertions.assertThat(
                        redirectTokensAuthenticationPersistedData.getRedirectTokens().isPresent())
                .isTrue();
        RedirectTokens redirectTokens =
                redirectTokensAuthenticationPersistedData.getRedirectTokens().get();
        Assertions.assertThat(redirectTokens.getRefreshToken().getBody())
                .isEqualTo(oAuth2Token.getRefreshToken().get().getBytes());
    }

    @Test
    public void shouldReturnEmptyAgentAuthenticationPersistedDataWhenThereIsNoOauth2TokenStored() {
        // given
        Mockito.when(persistentStorage.get(OAUTH2_STORAGE_KEY, OAuth2Token.class))
                .thenReturn(Optional.empty());
        Mockito.when(persistentStorage.containsKey(OAUTH2_STORAGE_KEY)).thenReturn(false);
        // when
        AgentAuthenticationPersistedData result = objectUnderTest.migrate(persistentStorage);
        // then
        Assertions.assertThat(result.valuesCopy().isEmpty()).isTrue();
    }

    private void mockAccessToken() {
        Mockito.when(oAuth2Token.getAccessExpireEpoch()).thenReturn(360l);
        Mockito.when(oAuth2Token.getAccessToken()).thenReturn("dummyAccessTokenBody");
        Mockito.when(oAuth2Token.getIssuedAt()).thenReturn(System.currentTimeMillis() / 1000);
    }
}
