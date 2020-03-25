package se.tink.backend.aggregation.nxgen.controllers.authentication.oauth;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class OAuth2AuthorizationSpecificationTest {

    private static final String CLIENT_ID = "someClientId";
    private static final String REDIRECT_URL = "http://127.0.0.1";
    private static final String AUTHORIZATION_URL = "http://authorization.server.com";
    private static final String ACCESS_TOKEN_ENDPOINT =
            "http://authorization.server.com/iisueToken";
    private OAuth2AuthorizationSpecification.Builder objectUnderTest;

    @Before
    public void init() {
        this.objectUnderTest = createBuilderFullFilledWithRequiredSet();
    }

    @Test
    public void builderShouldCreateMinimalFilledSpecificationWithTokenResponseType() {
        // when
        OAuth2AuthorizationSpecification result = objectUnderTest.build();
        // then
        Assert.assertEquals(CLIENT_ID, result.getClientId());
        Assert.assertEquals(REDIRECT_URL, result.getRedirectUrl().toString());
        Assert.assertEquals(
                AUTHORIZATION_URL, result.getAuthenticationEndpoint().getUrl().toString());
        Assert.assertEquals("token", result.getResponseType());
    }

    @Test
    public void builderShouldCreateMinimalFilledSpecificationWithCodeResponseType() {
        // given
        objectUnderTest.withResponseTypeCode(new EndpointSpecification(ACCESS_TOKEN_ENDPOINT));
        // when
        OAuth2AuthorizationSpecification result = objectUnderTest.build();
        // then
        Assert.assertEquals(CLIENT_ID, result.getClientId());
        Assert.assertEquals(REDIRECT_URL, result.getRedirectUrl().toString());
        Assert.assertEquals(
                AUTHORIZATION_URL, result.getAuthenticationEndpoint().getUrl().toString());
        Assert.assertEquals("code", result.getResponseType());
        Assert.assertEquals(
                ACCESS_TOKEN_ENDPOINT, result.getAccessTokenEndpoint().getUrl().toString());
    }

    @Test
    public void builderShouldCreateSpecificationWithScope() {
        // given
        objectUnderTest.withScopes("scope1", "scope2");
        // when
        OAuth2AuthorizationSpecification result = objectUnderTest.build();
        // then
        Assert.assertEquals("scope1 scope2", result.getScope().get());
    }

    @Test
    public void builderShouldCreateSpecificationWithAccessTokenRequestClientSpecificParameters() {
        // given
        objectUnderTest.withAccessTokenRequestClientSpecificParameter(
                "specificKey1", "specificValue1");
        objectUnderTest.withAccessTokenRequestClientSpecificParameter(
                "specificKey2", "specificValue2");
        // when
        OAuth2AuthorizationSpecification result = objectUnderTest.build();
        // then
        Assert.assertEquals(
                "specificValue1",
                result.getAccessTokenRequestClientSpecificParameters().get("specificKey1"));
        Assert.assertEquals(
                "specificValue2",
                result.getAccessTokenRequestClientSpecificParameters().get("specificKey2"));
    }

    @Test
    public void builderShouldCreateSpecificationWithAccessTokenResponseClientSpecificProperties() {
        // given
        objectUnderTest.withAccessTokenResponseClientSpecificProperty("specificKey1");
        objectUnderTest.withAccessTokenResponseClientSpecificProperty("specificKey2");
        // when
        OAuth2AuthorizationSpecification result = objectUnderTest.build();
        // then
        Assert.assertTrue(
                result.getAccessTokenResponseClientSpecificProperties().contains("specificKey1"));
        Assert.assertTrue(
                result.getAccessTokenResponseClientSpecificProperties().contains("specificKey2"));
    }

    @Test
    public void builderShouldCreateSpecificationWithDefaultTokenLifetime() {
        // given
        objectUnderTest.withDefaultAccessTokenLifetime(3600);
        // when
        OAuth2AuthorizationSpecification result = objectUnderTest.build();
        // then
        Assert.assertEquals(3600l, result.getDefaultAccessTokenLifetime().get().longValue());
    }

    @Test
    public void builderShouldCreateSpecificationWithRefreshTokenEndpoint() {
        // given
        final String refreshTokenEndpoint = "http://authorization.server.com/refreshToken";
        objectUnderTest.withTokenRefreshEndpoint(new EndpointSpecification(refreshTokenEndpoint));
        // when
        OAuth2AuthorizationSpecification result = objectUnderTest.build();
        // then
        Assert.assertEquals(
                refreshTokenEndpoint, result.getRefreshTokenEndpoint().getUrl().toString());
    }

    @Test(expected = IllegalStateException.class)
    public void builderShouldThrowExceptionWhenResponseTypeWasNotProvided() {
        // given
        objectUnderTest =
                new OAuth2AuthorizationSpecification.Builder()
                        .withClientId(CLIENT_ID)
                        .withRedirectUrl(REDIRECT_URL)
                        .withAuthenticationEndpoint(new EndpointSpecification(AUTHORIZATION_URL));
        // when
        objectUnderTest.build();
    }

    @Test(expected = IllegalStateException.class)
    public void builderShouldThrowExceptionWhenAuthorizationEndpointWasNotProvided() {
        // given
        objectUnderTest =
                new OAuth2AuthorizationSpecification.Builder()
                        .withClientId(CLIENT_ID)
                        .withRedirectUrl(REDIRECT_URL)
                        .withResponseTypeToken();
        // when
        objectUnderTest.build();
    }

    @Test(expected = IllegalStateException.class)
    public void builderShouldThrowExceptionWhenClientIdtWasNotProvided() {
        // given
        OAuth2AuthorizationSpecification.Builder objectUnderTest =
                new OAuth2AuthorizationSpecification.Builder()
                        .withRedirectUrl(REDIRECT_URL)
                        .withResponseTypeToken()
                        .withAuthenticationEndpoint(new EndpointSpecification(AUTHORIZATION_URL));
        // when
        objectUnderTest.build();
    }

    @Test(expected = IllegalStateException.class)
    public void builderShouldThrowExceptionWhenRedirectURLWasNotProvided() {
        // given
        OAuth2AuthorizationSpecification.Builder objectUnderTest =
                new OAuth2AuthorizationSpecification.Builder()
                        .withClientId(CLIENT_ID)
                        .withResponseTypeToken()
                        .withAuthenticationEndpoint(new EndpointSpecification(AUTHORIZATION_URL));
        // when
        objectUnderTest.build();
    }

    private OAuth2AuthorizationSpecification.Builder createBuilderFullFilledWithRequiredSet() {
        return new OAuth2AuthorizationSpecification.Builder()
                .withClientId(CLIENT_ID)
                .withRedirectUrl(REDIRECT_URL)
                .withResponseTypeToken()
                .withAuthenticationEndpoint(new EndpointSpecification(AUTHORIZATION_URL));
    }
}
