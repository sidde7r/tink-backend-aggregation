package se.tink.backend.main.resources;

import com.google.common.base.Objects;
import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.core.TinkUserAgent;

public class TinkUserAgentTest {

    @Test
    public void testMinVersionComparisions() {
        verifyMinInCorrectVersion(
                "Tink Mobile/1.7.5 (Android; 4.4.2, LGE Nexus 4)",
                null,
                "1.7.6");
        verifyMinCorrectVersion(
                "Tink Mobile/1.7.0-beta1-staging (iOS; 8.1.2, iPhone)",
                null,
                "1.6.2");
        verifyMinInCorrectVersion(
                "Tink Mobile/1.7.0-beta1-staging (iOS; 8.1.2, iPhone)",
                "10.0.0",
                "9.6.2");
        verifyMinCorrectVersion(
                "Tink Mobile/1.7.6 (iOS; 8.1, iPhone Simulator)",
                null,
                "1.7.6");
        verifyMinCorrectVersion(
                "Tink Mobile/1.7.8 (Android; 4.4.2, LGE Nexus 4)",
                null,
                "1.7.6");
        verifyMinCorrectVersion(
                "Tink Mobile/1.7.8 (iOS; 8.1, iPhone Simulator)",
                null,
                "1.7.6");
        verifyMinInCorrectVersion(
                "Tink Mobile/1.7 (iOS; 8.1, iPhone Simulator)",
                "1.7.6",
                null);
        verifyMinCorrectVersion(
                "Tink Mobile/1.7.2 (iOS; 8.1, iPhone Simulator)",
                "1.7",
                null);
        verifyMinInCorrectVersion(
                "Tink Mobile/1.4.4 (iOS; 8.1, iPhone Simulator)",
                "1.7",
                null);
        verifyMinCorrectVersion(
                "Tink Mobile/1.7.4 (iOS; 8.1, iPhone Simulator)",
                "1.7.3 Beta",
                null);
        verifyMinCorrectVersion(
                "Tink Mobile/1.7.4 (iOS; 8.1, iPhone Simulator)",
                "1.7.3-B",
                null);
        verifyMinInCorrectVersion(
                "Tink Mobile/1.1.14 (iOS; 8.1, iPhone Simulator)",
                "1.1.15",
                null);
        verifyMinCorrectVersion(
                null,
                "1.1.15",
                null);
        verifyMinCorrectVersion(
                "Tink Mobile/1.8.10 (Android; 4.4.2, LGE Nexus 4)",
                null,
                "1.8.5");
        verifyMinCorrectVersion(
                "Tink Mobile/1.1.20 (iOS; 8.1, iPhone Simulator)",
                "1.1.8",
                null);
    }

    @Test
    public void testMaxVersionComparisions() {
        verifyMaxInCorrectVersion(
                "Tink Mobile/1.7.6 (Android; 4.4.2, LGE Nexus 4)",
                null,
                "1.7.5");
        verifyMaxCorrectVersion(
                "Tink Mobile/1.7.6 (iOS; 8.1, iPhone Simulator)",
                null,
                "1.7.6");
        verifyMaxCorrectVersion(
                "Tink Mobile/1.7.6 (Android; 4.4.2, LGE Nexus 4)",
                null,
                "1.7.8");
        verifyMaxCorrectVersion(
                "Tink Mobile/1.7.6 (iOS; 8.1, iPhone Simulator)",
                null,
                "1.7.8");
        verifyMaxInCorrectVersion(
                "Tink Mobile/1.7.6 (iOS; 8.1, iPhone Simulator)",
                "1.7",
                null);
        verifyMaxCorrectVersion(
                "Tink Mobile/1.7 (iOS; 8.1, iPhone Simulator)",
                "1.7.2",
                null);
        verifyMaxInCorrectVersion(
                "Tink Mobile/1.7 (iOS; 8.1, iPhone Simulator)",
                "1.4.4",
                null);
        verifyMaxCorrectVersion(
                "Tink Mobile/1.7.3 (iOS; 8.1, iPhone Simulator)",
                "1.7.4 Beta",
                null);
        verifyMaxCorrectVersion(
                "Tink Mobile/1.7.3 (iOS; 8.1, iPhone Simulator)",
                "1.7.4-B",
                null);
        verifyMaxInCorrectVersion(
                "Tink Mobile/1.1.15 (iOS; 8.1, iPhone Simulator)",
                "1.1.14",
                null);
        verifyMaxCorrectVersion(
                null,
                "1.1.15",
                null);
        verifyMaxCorrectVersion(
                "Tink Mobile/1.8.5 (Android; 4.4.2, LGE Nexus 4)",
                null,
                "1.8.10");
        verifyMaxCorrectVersion(
                "Tink Mobile/1.1.8 (iOS; 8.1, iPhone Simulator)",
                "1.1.20",
                null);
    }

    @Test
    public void testParticularFuckedClientCase() {
        TinkUserAgent userAgent = new TinkUserAgent("Tink Mobile/2.0.0 (iOS; 8.1.2, iPhone)");
        Assert.assertTrue(userAgent.hasValidVersion("2.0.0", "2.0.0", null, null));

        userAgent = new TinkUserAgent("Tink Mobile/2.0.1 (iOS; 8.1.2, iPhone)");
        Assert.assertFalse(userAgent.hasValidVersion("2.0.0", "2.0.0", null, null));
    }

    @Test
    public void testTink25BetaVersion() {
        TinkUserAgent ua = new TinkUserAgent("Tink Mobile/2.5-Beta-2-Production (iOS; 9.2, iPhone)");

        Assert.assertTrue(ua.hasValidVersion("2.5.0", null, null, null));
        Assert.assertTrue(ua.hasValidVersion("2.0.5", null, null, null));
        Assert.assertTrue(ua.hasValidVersion("2.4.999", null, null, null));
        Assert.assertFalse(ua.hasValidVersion("2.5.1", null, null, null));

        Assert.assertFalse(ua.hasValidVersion(null, "2.4.99999", null, null));
        Assert.assertFalse(ua.hasValidVersion(null, "2.0.5", null, null));
        Assert.assertTrue(ua.hasValidVersion(null, "3.0.0", null, null));
        Assert.assertTrue(ua.hasValidVersion(null, "2.5.1", null, null));
        Assert.assertTrue(ua.hasValidVersion(null, "2.5.0", null, null));

        Assert.assertEquals("Beta-2-Production", ua.getSpecialReleaseType());
    }
    @Test
    public void testTink25() {
        TinkUserAgent ua = new TinkUserAgent("Tink Mobile/2.5 (iOS; 9.2, iPhone)");

        Assert.assertTrue(ua.hasValidVersion("2.5.0", null, null, null));
        Assert.assertTrue(ua.hasValidVersion("2.0.5", null, null, null));
        Assert.assertTrue(ua.hasValidVersion("2.4.999", null, null, null));
        Assert.assertFalse(ua.hasValidVersion("2.5.1", null, null, null));

        Assert.assertFalse(ua.hasValidVersion(null, "2.4.99999", null, null));
        Assert.assertFalse(ua.hasValidVersion(null, "2.0.5", null, null));
        Assert.assertTrue(ua.hasValidVersion(null, "3.0.0", null, null));
        Assert.assertTrue(ua.hasValidVersion(null, "2.5.1", null, null));
        Assert.assertTrue(ua.hasValidVersion(null, "2.5.0", null, null));
    }

    @Test
    public void testTinkIos250() {
        TinkUserAgent ua = new TinkUserAgent("Tink Mobile/2.5.0 (iOS; 9.2, iPhone)");

        Assert.assertTrue(ua.hasValidVersion("2.5.0", null, null, null));
        Assert.assertTrue(ua.hasValidVersion("2.0.5", null, null, null));
        Assert.assertTrue(ua.hasValidVersion("2.4.999", null, null, null));
        Assert.assertFalse(ua.hasValidVersion("2.5.1", null, null, null));

        Assert.assertFalse(ua.hasValidVersion(null, "2.4.99999", null, null));
        Assert.assertFalse(ua.hasValidVersion(null, "2.0.5", null, null));
        Assert.assertTrue(ua.hasValidVersion(null, "3.0.0", null, null));
        Assert.assertTrue(ua.hasValidVersion(null, "2.5.1", null, null));
        Assert.assertTrue(ua.hasValidVersion(null, "2.5.0", null, null));
    }

    @Test
    public void testIosTransferBetaVersion() {
        TinkUserAgent iosUa = new TinkUserAgent("Tink Mobile/2.5.0 (iOS; 9.2, iPhone)");
        TinkUserAgent androidUa = new TinkUserAgent("Tink Mobile/1.7.6 (Android; 4.4.2, LGE Nexus 4)");

        Assert.assertTrue(Objects.equal(iosUa.getOs(), "ios") && iosUa.hasValidVersion("2.5.0", null));
        Assert.assertFalse(Objects.equal(androidUa.getOs(), "ios") && androidUa.hasValidVersion("2.5.0", null));
    }

    @Test
    public void testTinkAndroid250() {
        TinkUserAgent ua = new TinkUserAgent("Tink Mobile/2.5.0 (Android; 4.4.2, LGE Nexus 4)");

        Assert.assertTrue(ua.hasValidVersion(null, null, "2.5.0", null));
        Assert.assertTrue(ua.hasValidVersion(null, null, "2.0.5", null));
        Assert.assertTrue(ua.hasValidVersion(null, null, "2.4.999", null));
        Assert.assertFalse(ua.hasValidVersion(null, null, "2.5.1", null));

        Assert.assertFalse(ua.hasValidVersion(null, null, null, "2.4.99999"));
        Assert.assertFalse(ua.hasValidVersion(null, null, null, "2.0.5"));
        Assert.assertTrue(ua.hasValidVersion(null, null, null, "3.0.0"));
        Assert.assertTrue(ua.hasValidVersion(null, null, null, "2.5.1"));
        Assert.assertTrue(ua.hasValidVersion(null, null, null, "2.5.0"));
    }

    @Test
    public void testAndroidTransferBetaVersion() {
        TinkUserAgent androidUa = new TinkUserAgent("Tink Mobile/2.5.0 (Android; 4.4.2, LGE Nexus 4)");
        TinkUserAgent iosUa = new TinkUserAgent("Tink Mobile/2.5.0 (iOS; 9.2, iPhone)");

        Assert.assertTrue(Objects.equal(androidUa.getOs(), "android") && androidUa.hasValidVersion(null, "2.5.0"));
        Assert.assertFalse(Objects.equal(iosUa.getOs(), "android") && iosUa.hasValidVersion(null, "2.5.0"));

    }
    
    @Test
    public void testVersionRange1() {
        TinkUserAgent userAgent = new TinkUserAgent("Tink Mobile/1.7.6 (Android; 4.4.2, LGE Nexus 4)");
        Assert.assertTrue(userAgent.hasValidVersion(null, null, "1.7.0", "1.7.9"));
    }

    @Test
    public void testVersionRange2() {
        TinkUserAgent userAgent = new TinkUserAgent("Tink Mobile/1.7.0 (Android; 4.4.2, LGE Nexus 4)");
        Assert.assertTrue(userAgent.hasValidVersion(null, null, "1.7.0", "1.7.9"));
    }

    @Test
    public void testVersionRange3() {
        TinkUserAgent userAgent = new TinkUserAgent("Tink Mobile/1.7.9 (Android; 4.4.2, LGE Nexus 4)");
        Assert.assertTrue(userAgent.hasValidVersion(null, null, "1.7.0", "1.7.9"));
    }

    @Test
    public void testVersionRange4() {
        TinkUserAgent userAgent = new TinkUserAgent("Tink Mobile/1.7.10 (Android; 4.4.2, LGE Nexus 4)");
        Assert.assertFalse(userAgent.hasValidVersion(null, null, "1.7.0", "1.7.9"));
    }

    @Test
    public void testVersionRange5() {
        TinkUserAgent userAgent = new TinkUserAgent("Tink Mobile/2.0.0 (Android; 4.4.2, LGE Nexus 4)");
        Assert.assertFalse(userAgent.hasValidVersion(null, null, "1.7.0", "1.7.9"));
    }

    private void verifyMinCorrectVersion(String userAgentDescription, String minIosVersion, String minAndroidVersion) {
        TinkUserAgent userAgent = new TinkUserAgent(userAgentDescription);
        Assert.assertTrue(userAgent.hasValidVersion(minIosVersion, minAndroidVersion));
    }

    private void verifyMinInCorrectVersion(String userAgentDescription, String minIosVersion, String minAndroid) {
        TinkUserAgent userAgent = new TinkUserAgent(userAgentDescription);
        Assert.assertFalse(userAgent.hasValidVersion(minIosVersion, minAndroid));
    }

    private void verifyMaxCorrectVersion(String userAgentDescription, String maxIosVersion, String maxAndroidVersion) {
        TinkUserAgent userAgent = new TinkUserAgent(userAgentDescription);
        Assert.assertTrue(userAgent.hasValidVersion(null, maxIosVersion, null, maxAndroidVersion));
    }

    private void verifyMaxInCorrectVersion(String userAgentDescription, String maxIosVersion, String maxAndroidVersion) {
        TinkUserAgent userAgent = new TinkUserAgent(userAgentDescription);
        Assert.assertFalse(userAgent.hasValidVersion(null, maxIosVersion, null, maxAndroidVersion));
    }

    @Test
    public void testSpecialReleaseType() {
        TinkUserAgent ua1 = new TinkUserAgent ("Tink Mobile/1.9.0 (Android; 4.4.2, LGE Nexus 4)");
        TinkUserAgent ua2 = new TinkUserAgent ("Tink Mobile/1.9.0 Beta (Android; 4.4.2, LGE Nexus 4)");
        TinkUserAgent ua3 = new TinkUserAgent ("Tink Mobile/1.9.0 Alpha (Android; 4.4.2, LGE Nexus 4)");
        TinkUserAgent ua4 = new TinkUserAgent ("Tink Mobile/1.9.0-Beta (Android; 4.4.2, LGE Nexus 4)");
        TinkUserAgent ua5 = new TinkUserAgent ("Tink Mobile/1.9.0-B (Android; 4.4.2, LGE Nexus 4)");
        TinkUserAgent ua6 = new TinkUserAgent ("Tink Mobile/1.9.0 Special (Android; 4.4.2, LGE Nexus 4)");
        TinkUserAgent ua7 = new TinkUserAgent ("Tink Mobile/1.9 (Android; 4.4.2, LGE Nexus 4)");
        TinkUserAgent ua8 = new TinkUserAgent ("Tink Mobile/2.0.215125 beta (Android; 4.4.2, LGE Nexus 4)");

        Assert.assertEquals("1.9.0", ua1.getAppVersion());
        Assert.assertEquals("1.9.0", ua2.getAppVersion());
        Assert.assertEquals("1.9.0", ua3.getAppVersion());
        Assert.assertEquals("1.9.0", ua4.getAppVersion());
        Assert.assertEquals("1.9.0", ua5.getAppVersion());
        Assert.assertEquals("1.9.0", ua6.getAppVersion());
        Assert.assertEquals("1.9", ua7.getAppVersion());
        Assert.assertEquals("2.0.215125", ua8.getAppVersion());

        // special release type
        Assert.assertNull(ua1.getSpecialReleaseType());
        Assert.assertEquals("Beta", ua2.getSpecialReleaseType());
        Assert.assertEquals("Alpha", ua3.getSpecialReleaseType());
        Assert.assertEquals("Beta", ua4.getSpecialReleaseType());
        Assert.assertEquals("B", ua5.getSpecialReleaseType());
        Assert.assertEquals("Special", ua6.getSpecialReleaseType());
        Assert.assertNull(ua7.getSpecialReleaseType());
        Assert.assertEquals("beta", ua8.getSpecialReleaseType());

        // is beta
        Assert.assertFalse(ua1.isBetaVersion());
        Assert.assertTrue(ua2.isBetaVersion());
        Assert.assertFalse(ua3.isBetaVersion());
        Assert.assertTrue(ua4.isBetaVersion());
        Assert.assertFalse(ua5.isBetaVersion());
        Assert.assertFalse(ua6.isBetaVersion());
        Assert.assertFalse(ua7.isBetaVersion());
        Assert.assertTrue(ua8.isBetaVersion());
    }

    @Test
    public void testOsVersionParsing() {
        Assert.assertEquals("10.3.2", new TinkUserAgent("Grip/1.8.0 (iOS; 10.3.2, iPhone)").getOsVersion());
        Assert.assertEquals("4.4.2",
                new TinkUserAgent("Tink Mobile/2.0.215125 beta (Android; 4.4.2, LGE Nexus 4)").getOsVersion());
        Assert.assertEquals("4.4.2",
                new TinkUserAgent("Tink Mobile/1.9.0 Alpha (Android; 4.4.2, LGE Nexus 4)").getOsVersion());
        Assert.assertEquals("9.8.1", new TinkUserAgent("Grip/2.0.0 (iOS; 9.8.1, iPhone)").getOsVersion());
    }

    @Test
    public void testOsVersionMinimumVersion() {
        Assert.assertTrue(new TinkUserAgent("Grip/1.8.0 (iOS; 10.3.2, iPhone)").hasMinimumOsVersion("10.0.0"));
        Assert.assertTrue(new TinkUserAgent("Grip/2.0.0 (iOS; 10.0.0, iPhone)").hasMinimumOsVersion("10.0.0"));
        Assert.assertFalse(new TinkUserAgent("Grip/2.0.0 (iOS; 10.0.0, iPhone)").hasMinimumOsVersion("10.3.0"));
        Assert.assertFalse(new TinkUserAgent("Grip/2.0.0 (iOS; 9.0.0, iPhone)").hasMinimumOsVersion("10.0.0"));
        Assert.assertFalse(new TinkUserAgent("Grip/2.0.0 (iOS; 9.9.9, iPhone)").hasMinimumOsVersion("10.0.0"));
    }

    @Test
    public void testIsIOS() {
        Assert.assertTrue(new TinkUserAgent("Grip/1.8.0 (iOS; 10.3.2, iPhone)").isIOS());
        Assert.assertFalse(new TinkUserAgent("Tink Mobile/1.9.0 Alpha (Android; 4.4.2, LGE Nexus 4)").isIOS());
    }
}
