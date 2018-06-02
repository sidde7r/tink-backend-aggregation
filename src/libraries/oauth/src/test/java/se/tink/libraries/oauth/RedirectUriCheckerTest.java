package se.tink.libraries.oauth;

import org.junit.Test;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;

public class RedirectUriCheckerTest {

    @Test
    public void httpOrHttpsIsOkayForLocalhost() {
        assertTrue(RedirectUriChecker.validate("http://localhost:1234"));
        assertTrue(RedirectUriChecker.validate("https://localhost:1234"));
        assertTrue(RedirectUriChecker.validate("http://localhost:3000/callback"));
        assertTrue(RedirectUriChecker.validate("http://localhost:1111/another/callback"));

        assertTrue(RedirectUriChecker.validate("https://localhost:1234"));
        assertTrue(RedirectUriChecker.validate("https://localhost:3000/callback"));
        assertTrue(RedirectUriChecker.validate("https://localhost:1111/another/callback"));
    }

    @Test
    public void invalidUriIsNotAllowed() {
        assertFalse(RedirectUriChecker.validate("https://mycompany callback"));
        assertFalse(RedirectUriChecker.validate("https://mycompany|callback"));
    }

    @Test
    public void httpsAndCustomSchemeIsAllowedForNonLocalhost() {
        assertTrue(RedirectUriChecker.validate("https://mycompany.com"));
        assertTrue(RedirectUriChecker.validate("https://mycompany.com/"));
        assertTrue(RedirectUriChecker.validate("https://mycompany.com/callback"));
        assertTrue(RedirectUriChecker.validate("https://mycompany.com/a/b/c/d"));
        assertTrue(RedirectUriChecker.validate("https://otherdomain.com"));

        assertTrue(RedirectUriChecker.validate("myapp://tink/callback"));
        assertTrue(RedirectUriChecker.validate("someothername://callback/for/the/code"));
    }
}
