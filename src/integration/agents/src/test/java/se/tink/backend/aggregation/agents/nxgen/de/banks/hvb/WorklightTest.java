package se.tink.backend.aggregation.agents.nxgen.de.banks.hvb;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.worklight.AuthenticityChallengeHandler;

public class WorklightTest {
    private enum Arg {
        CHALLENGE_DATA,
        AUTHENTICITY_REALM,
    }

    private final ArgumentManager<Arg> helper = new ArgumentManager<>(Arg.values());

    @Before
    public void before() {
        helper.before();
    }

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }

    @Test
    public void testAuthenticityChallengeHandler() {
        // First argument to PSecurityUtils::answerChallenge:
        // Roughly 154-character string containing one '+' and two '-' near the end
        String returned =
                AuthenticityChallengeHandler.challengeToAuthenticityRealmString(
                        helper.get(Arg.CHALLENGE_DATA),
                        HVBConstants.MODULE_NAME,
                        HVBConstants.APP_ID);
        // Return value of PSecurityUtils::answerChallenge:
        // Roughly 37-character base64-encoded string
        String expected = helper.get(Arg.AUTHENTICITY_REALM);
        Assert.assertEquals(expected, returned);
    }
}
