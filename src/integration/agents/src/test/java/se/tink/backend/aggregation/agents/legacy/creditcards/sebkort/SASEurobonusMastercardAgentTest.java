package se.tink.backend.aggregation.agents.creditcards.sebkort;

import org.junit.Test;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.agents.AbstractAgentTest;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.libraries.social.security.TestSSN;

public class SASEurobonusMastercardAgentTest extends AbstractAgentTest<SEBKortAgent> {
    public SASEurobonusMastercardAgentTest() {
        super(SEBKortAgent.class);
    }

    @Test
    public void testUser1() throws Exception {
        testAgent("198206157615", "breg0tt");
    }

    @Test
    public void testUser2Password() throws Exception {
        testAgent(TestSSN.FH, "gablanko24", CredentialsTypes.PASSWORD);
    }

    @Test
    public void testUser1AuthenticationError() throws Exception {
        testAgentAuthenticationError(TestSSN.FH, "gablanko25");
    }

    @Test
    public void testUser2MobileBankId() throws Exception {
        testAgent(TestSSN.FH, null, CredentialsTypes.MOBILE_BANKID);
    }

    @Test
    public void testJohannesMobileBankId() throws Exception {
        testAgent("198709230356", null, CredentialsTypes.MOBILE_BANKID);
    }

    @Override
    protected Provider constructProvider() {
        Provider provider = new Provider();

        provider.setPayload("sase:0102");

        return provider;
    }
}
