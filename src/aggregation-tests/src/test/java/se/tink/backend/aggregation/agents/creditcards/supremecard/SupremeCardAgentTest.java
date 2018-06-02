package se.tink.backend.aggregation.agents.creditcards.supremecard;

import org.junit.Test;
import se.tink.backend.aggregation.agents.AbstractAgentTest;
import se.tink.backend.aggregation.rpc.CredentialsTypes;
import se.tink.backend.common.utils.TestSSN;

public class SupremeCardAgentTest extends AbstractAgentTest<SupremeCardAgent> {
    public SupremeCardAgentTest() {
        super(SupremeCardAgent.class);
    }

    @Test
    public void testUser1AuthenticationError() throws Exception {
        testAgentAuthenticationError(TestSSN.FH, "testtest");
    }

    @Test
    public void testStartSession() throws Exception {
        testAgent(TestSSN.AL, null, CredentialsTypes.MOBILE_BANKID);
    }
}
