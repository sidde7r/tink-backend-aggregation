package se.tink.backend.aggregation.nxgen.controllers.authentication.oauth;

import org.junit.Assert;
import org.junit.Test;

public class OAuth2TokenTest {

    @Test
    public void tokenShouldNotBeExpired() {
        // given
        OAuth2Token objectUnderTest = new OAuth2Token();
        objectUnderTest.setExpiresIn(3600l);
        // when
        boolean result = objectUnderTest.hasAccessExpired();
        // then
        Assert.assertFalse(result);
    }

    @Test
    public void tokenShouldBeExpired() {
        // given
        OAuth2Token objectUnderTest = new OAuth2Token();
        objectUnderTest.setExpiresIn(-3600l);
        // when
        boolean result = objectUnderTest.hasAccessExpired();
        // then
        Assert.assertTrue(result);
    }

    @Test
    public void tokenShouldBeValid() {
        // given
        OAuth2Token objectUnderTest = new OAuth2Token();
        objectUnderTest.setExpiresIn(3600l);
        // when
        boolean result = objectUnderTest.isValid();
        // then
        Assert.assertTrue(result);
    }

    @Test
    public void tokenShouldBeNotValid() {
        // given
        OAuth2Token objectUnderTest = new OAuth2Token();
        objectUnderTest.setExpiresIn(-3600l);
        // when
        boolean result = objectUnderTest.isValid();
        // then
        Assert.assertFalse(result);
    }

    @Test
    public void canRefresh() {
        // given
        OAuth2Token objectUnderTest = new OAuth2Token();
        objectUnderTest.setRefreshToken("423543543534");
        // when
        boolean result = objectUnderTest.canRefresh();
        // then
        Assert.assertTrue(result);
    }

    @Test
    public void canNotRefresh() {
        // given
        OAuth2Token objectUnderTest = new OAuth2Token();
        // when
        boolean result = objectUnderTest.canRefresh();
        // then
        Assert.assertFalse(result);
    }
}
