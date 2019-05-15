package se.tink.backend.aggregation.agents.creditcards.supremecard;

import org.junit.Test;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.aggregation.agents.AbstractAgentTest;
import se.tink.libraries.social.security.TestSSN;

public class SupremeCardLegacyAgentTest extends AbstractAgentTest<SupremeCardAgent> {
    public SupremeCardLegacyAgentTest() {
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
