package se.tink.backend.aggregation.nxgen.controllers.authentication.oauth;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class OAuth2TokenTypeTokenIssueStrategyTest {

    private OAuth2AuthorizationSpecification authorizationParamProvider;
    private OAuth2TokenTypeTokenIssueStrategy objectUnderTest;

    @Before
    public void init() {
        authorizationParamProvider = Mockito.mock(OAuth2AuthorizationSpecification.class);
        objectUnderTest = new OAuth2TokenTypeTokenIssueStrategy(authorizationParamProvider);
    }

    @Test
    public void shouldIssuedToken() {
        // given
        final String accessToken = "2142543fsf34r";
        final String tokenType = "example_type";
        final String expiresIn = "3600";
        final String scope = "example_scope";
        Map<String, String> authorizationCallbackData = new HashMap<>();
        authorizationCallbackData.put("access_token", accessToken);
        authorizationCallbackData.put("token_type", tokenType);
        authorizationCallbackData.put("expires_in", expiresIn);
        authorizationCallbackData.put("scope", scope);
        // when
        OAuth2Token result = objectUnderTest.issueToken(authorizationCallbackData);
        // then
        Assert.assertEquals(accessToken, result.getAccessToken());
        Assert.assertEquals(tokenType, result.getTokenType());
        Assert.assertEquals(new Long(expiresIn), result.getExpiresIn());
        Assert.assertEquals(scope, result.getScope());
    }

    @Test
    public void shouldUseDefaultTokenLifetimeWhenAuthorizationServerDidNotReturnIt() {
        // given
        Mockito.when(authorizationParamProvider.getDefaultAccessTokenLifetime())
                .thenReturn(Optional.of(7200l));
        final String accessToken = "2142543fsf34r";
        final String tokenType = "example_type";
        final String scope = "example_scope";
        Map<String, String> authorizationCallbackData = new HashMap<>();
        authorizationCallbackData.put("access_token", accessToken);
        authorizationCallbackData.put("token_type", tokenType);
        authorizationCallbackData.put("scope", scope);
        // when
        OAuth2Token result = objectUnderTest.issueToken(authorizationCallbackData);
        // then
        Assert.assertEquals(
                authorizationParamProvider.getDefaultAccessTokenLifetime().get(),
                result.getExpiresIn());
    }

    @Test(expected = RuntimeException.class)
    public void
            shouldThrowRuntimeExceptionWhenAccessTokenLifetimeIsNotPresentNeitherInAuthorizationResponseNeitherInAuthorizationParamsProvider() {
        // given
        Mockito.when(authorizationParamProvider.getDefaultAccessTokenLifetime())
                .thenReturn(Optional.empty());
        final String accessToken = "2142543fsf34r";
        final String tokenType = "example_type";
        final String scope = "example_scope";
        Map<String, String> authorizationCallbackData = new HashMap<>();
        authorizationCallbackData.put("access_token", accessToken);
        authorizationCallbackData.put("token_type", tokenType);
        authorizationCallbackData.put("scope", scope);
        // when
        OAuth2Token result = objectUnderTest.issueToken(authorizationCallbackData);
    }
}
