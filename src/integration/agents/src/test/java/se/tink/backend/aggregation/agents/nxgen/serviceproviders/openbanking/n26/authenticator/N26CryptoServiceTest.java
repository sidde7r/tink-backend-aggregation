package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.authenticator;

import java.util.regex.Pattern;
import org.junit.Assert;
import org.junit.Test;

public class N26CryptoServiceTest {

    private static final N26CryptoService n26CryptoService = new N26CryptoService();

    @Test
    public void testCodeChallenge() {
        Assert.assertEquals(
                "gd9DfflBCHB3vSSaH1buWpOSjU6orw1ZC2qTyrtRy60",
                n26CryptoService.generateCodeChallenge(
                        ".B1he-YdPVkc__fmWVyoYtgMAKz2GFzlrQKSuuxjFNgc"));
    }

    @Test
    public void testCodeVerifierLength() {
        String codeVerifier = n26CryptoService.generateCodeVerifier();
        System.out.println(codeVerifier);
        Assert.assertTrue(codeVerifier.length() <= 128 && codeVerifier.length() >= 43);
    }

    @Test
    public void testCodeVerifierRegex() {
        String codeVerifier = n26CryptoService.generateCodeVerifier();
        Assert.assertTrue(Pattern.matches("[a-zA-Z0-9_.\\-~]*", codeVerifier));
    }
}
