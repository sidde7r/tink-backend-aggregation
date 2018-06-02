package se.tink.backend.auth;

import org.junit.Test;
import se.tink.libraries.auth.HttpAuthenticationMethod;
import se.tink.backend.utils.StringUtils;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AuthenticationDetailsTest {

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionIfReadingCredentialsOnInvalidDetails() {
        AuthenticationDetails details = new AuthenticationDetails("something-invalid");
        details.getAuthorizationCredentials();
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionIfReadingMethodOnInvalidDetails() {
        AuthenticationDetails details = new AuthenticationDetails("91y12iuh");
        details.getMethod();
    }

    @Test
    public void verifyBasicAuthIsParseable() {
        AuthenticationDetails details = new AuthenticationDetails("Basic amVsZ2hAdGluay5zZTpteS1zZWNyZXQ=");

        assertTrue(details.isValid());
        assertEquals(HttpAuthenticationMethod.BASIC, details.getMethod());
        assertEquals("amVsZ2hAdGluay5zZTpteS1zZWNyZXQ=", details.getAuthorizationCredentials());
    }

    @Test
    public void verifySessionIsParseable() {
        String sessionId = StringUtils.generateUUID();
        AuthenticationDetails details = new AuthenticationDetails(String.format("Session %s", sessionId));

        assertTrue(details.isValid());
        assertEquals(HttpAuthenticationMethod.SESSION, details.getMethod());
        assertEquals(sessionId, details.getAuthorizationCredentials());
        assertEquals(details.getSessionId().get(), sessionId);
    }

    @Test
    public void verifyBearerIsParseable() {
        String token = StringUtils.generateUUID();
        AuthenticationDetails details = new AuthenticationDetails(String.format("Bearer %s", token));

        assertTrue(details.isValid());
        assertEquals(HttpAuthenticationMethod.BEARER, details.getMethod());
        assertEquals(token, details.getAuthorizationCredentials());
    }

    @Test
    public void verifyFacebookIsParseable() {
        String token = "wajdwh921h9dh129d 129dh21iod j012ej 09812je 9821jr08j12r214-214+12+421 €?";
        AuthenticationDetails details = new AuthenticationDetails(String.format("Facebook %s", token));

        assertTrue(details.isValid());
        assertEquals(HttpAuthenticationMethod.FACEBOOK, details.getMethod());
        assertEquals(token, details.getAuthorizationCredentials());
    }
}
