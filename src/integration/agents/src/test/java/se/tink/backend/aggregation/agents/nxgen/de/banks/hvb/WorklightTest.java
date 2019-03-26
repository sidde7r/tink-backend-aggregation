package se.tink.backend.aggregation.agents.nxgen.de.banks.hvb;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.ArgumentHelper;
import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.worklight.AuthenticityChallengeHandler;

public class WorklightTest {
    private final ArgumentHelper helper;

    public WorklightTest() {
        helper = new ArgumentHelper("tink.challengeData", "tink.authenticityRealm");
    }

    @Before
    public void before() {
        helper.before();
    }

    @AfterClass
    public static void afterClass() {
        ArgumentHelper.afterClass();
    }

    @Test
    public void testAuthenticityChallengeHandler() {
        // First argument to PSecurityUtils::answerChallenge:
        // Roughly 154-character string containing one '+' and two '-' near the end
        String returned =
                AuthenticityChallengeHandler.challengeToAuthenticityRealmString(
                        helper.get("tink.challengeData"),
                        HVBConstants.MODULE_NAME,
                        HVBConstants.APP_ID);
        // Return value of PSecurityUtils::answerChallenge:
        // Roughly 37-character base64-encoded string
        String expected = helper.get("tink.authenticityRealm");
        Assert.assertEquals(expected, returned);
    }
}
