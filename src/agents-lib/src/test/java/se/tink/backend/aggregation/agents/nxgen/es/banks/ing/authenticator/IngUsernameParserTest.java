package se.tink.backend.aggregation.agents.nxgen.es.banks.ing.authenticator;

import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.aggregation.agents.exceptions.LoginException;

public class IngUsernameParserTest {

    @Test
    public void assertValidNIF_returns0() throws LoginException {
        int usernameType = IngAuthenticator.getUsernameType("12345678Z");
        Assert.assertEquals(0, usernameType);
    }

    @Test
    public void assertValidNIE_returns1() throws LoginException {
        int usernameType = IngAuthenticator.getUsernameType("Y2345678Z");
        Assert.assertEquals(1, usernameType);
    }

    @Test
    public void assertValidPassport_returns2() throws LoginException {
        int usernameType = IngAuthenticator.getUsernameType("XY123456");
        Assert.assertEquals(2, usernameType);
    }

    @Test(expected = LoginException.class)
    public void assertIncorrectUsername_throwsException() throws LoginException {
        int usernameType = IngAuthenticator.getUsernameType("XYZ123456S");
    }
}
