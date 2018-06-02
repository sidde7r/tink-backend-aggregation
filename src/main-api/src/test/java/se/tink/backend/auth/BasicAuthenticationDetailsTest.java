package se.tink.backend.auth;

import com.sun.jersey.core.util.Base64;
import org.junit.Assert;
import org.junit.Test;
import com.google.common.base.Charsets;

public class BasicAuthenticationDetailsTest {

    private static String base64Encode(String cleartext) {
        return new String(Base64.encode(cleartext.getBytes(Charsets.ISO_8859_1)));
    }

    @Test
    public void testAlfanumericPassword() {
        testUsernamePassword("jens.rantil@gmail.com", "ErrareHumanumEst");
    }

    @Test
    public void testComplexPassword() {
        testUsernamePassword("jens.rantil@gmail.com", "abc::::sdsa>:SDAåÅäÄöÖ");
    }

    @Test
    public void testEmptyPassword() {
        testUsernamePassword("jens.rantil@gmail.com", "");
    }

    @Test
    public void testMissingPassword() {
        boolean thrown = false;
        try {
            new BasicAuthenticationDetails(base64Encode("jens.rantil@tink.se"));
        } catch (IllegalArgumentException e) {
            // expected
            thrown = true;
        }
        Assert.assertTrue(thrown);
    }

    @Test
    public void testMultipleColons() {
        testUsernamePassword("jens.rantil@gmail.com", "::::::::");
    }

    @Test
    public void testNullInput() {
        boolean thrown = false;
        try {
            new BasicAuthenticationDetails(null);
        } catch (NullPointerException e) {
            // expected
            thrown = true;
        }
        Assert.assertTrue(thrown);
    }

    @Test
    public void testPasswordWithSymbols() {
        testUsernamePassword("jens.rantil@gmail.com", "<>!#%&/()=?}{\\¶\"§,.:;*'");
    }

    @Test
    public void testSwedishPassword() {
        testUsernamePassword("jens.rantil@gmail.com", "Å dä i å va en ö.");
    }

    private void testUsernamePassword(String username, String password) {
        Assert.assertEquals(
                new BasicAuthenticationDetails(username, password),
                new BasicAuthenticationDetails(BasicAuthenticationDetailsTest.base64Encode(username + ":" + password)));
    }

    // Just a test to show that basic is broken for colon users. We expect this.
    @Test
    public void testUsernameWithColon() {
        String username = "jens:rantil@tink.se";
        String password = "ButSoftWhatLightThroughYonderWindowBreaks?";
        String expectedUsername = "jens";
        String expectedPassword = "rantil@tink.se:ButSoftWhatLightThroughYonderWindowBreaks?";

        Assert.assertEquals(new BasicAuthenticationDetails(expectedUsername,
                expectedPassword), new BasicAuthenticationDetails(BasicAuthenticationDetailsTest.base64Encode(username
                + ":" + password)));
    }

}
