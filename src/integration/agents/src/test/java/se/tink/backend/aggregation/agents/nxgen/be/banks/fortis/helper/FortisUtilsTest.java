package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.helper;

import org.junit.Assert;
import org.junit.Test;

public class FortisUtilsTest {

    @Test
    public void shouldCalculateProperChallenge() {
        String result =
                FortisUtils.calculateChallenge(
                        "8FI9mXUD/rzc/KphC/ABdXrfDvsWxjSeJamNG3gYB8M=",
                        "password",
                        "E1111111",
                        "602B517A6AE32854D2BF80BF62F9B1FAAF7DC727F3090824F8E2C59407CF8E11",
                        "EjCBc1JWg1eNSVZ4WTjZu0J");
        Assert.assertEquals("Challenge response", "06330390", result);
    }

    @Test
    public void shouldCalculateProperChallenge_2() {
        String result =
                FortisUtils.calculateChallenge(
                        "Ue/yOQIHv89K+5r2kOdR08+iWl7Goucxt9anFt7Aepk=",
                        "pasword",
                        "E1111111",
                        "CF8C15D98EE98EAF2CD9F71FA470BFE8A3AACCDF853F86DCAA37E9451D3FA41C",
                        "LyzyCTOSvaxNdRNjXIKvlbs");
        Assert.assertEquals("Challenge response", "82720033", result);
    }

    @Test
    public void shouldCalculateProperChallenge_3() {
        String result =
                FortisUtils.calculateChallenge(
                        "Vs/qUhZE5tsqGDMcD6Pp0ISXn5YcKz/GXvBu/bvSg78=",
                        "password",
                        "E1111111",
                        "DBC778211F50A954583F1229CE552622D2FC90C1D714E67D2546E3782B50FF4C",
                        "nX-sx1vSpNLrMJ7lwYuk9wV");

        Assert.assertEquals("Challenge response", "18656806", result);
    }
}
