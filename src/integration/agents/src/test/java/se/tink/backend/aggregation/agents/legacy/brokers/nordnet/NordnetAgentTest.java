package se.tink.backend.aggregation.agents.brokers.nordnet;

import org.junit.Test;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.aggregation.agents.AbstractAgentTest;
import se.tink.backend.aggregation.agents.brokers.NordnetAgent;
import se.tink.libraries.social.security.TestSSN;

public class NordnetAgentTest extends AbstractAgentTest<NordnetAgent> {
    public NordnetAgentTest() {
        super(NordnetAgent.class);
    }

    @Test
    public void testUser1() throws Exception {
        testAgent("fhedberg", "4fG-wk-X", CredentialsTypes.PASSWORD, false);
    }

    @Test
    public void testUser2() throws Exception {
        testAgent("danielkj", "55FhR@Mq", CredentialsTypes.PASSWORD, false);
    }

    @Test
    public void testUser() throws Exception {
        testAgent("xxxxxx", "xxxxxxxx", CredentialsTypes.PASSWORD, false);
    }

    @Test
    public void testBankID() throws Exception {
        testAgent(TestSSN.DL, null, CredentialsTypes.MOBILE_BANKID, true);
    }

    @Test
    public void testUser1AuthenticationError() throws Exception {
        testAgentAuthenticationError("fhedberg", "colal");
    }
}
