package se.tink.backend.aggregation.agents.creditcards.ikano.api;

import org.junit.Test;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.agents.AbstractAgentTest;
import se.tink.libraries.social.security.TestSSN;

public class IkanoApiAgentIntegrationTest extends AbstractAgentTest<IkanoApiAgent> {
    public IkanoApiAgentIntegrationTest() {
        super(IkanoApiAgent.class);
    }

    @Test
    public void testUser1() throws Exception {
        testAgent(TestSSN.AL, null);
    }

    @Override
    protected Provider constructProvider() {
        Provider p = new Provider();
        p.setPayload("PREEM");
        p.setCurrency("SEK");
        return p;
    }
}
