package se.tink.backend.aggregation.agents.brokers.lysa;

import org.junit.Test;
import se.tink.backend.aggregation.agents.AbstractAgentTest;
import se.tink.backend.aggregation.rpc.CredentialsTypes;
import se.tink.backend.aggregation.rpc.Provider;
import se.tink.libraries.social.security.TestSSN;

public class LysaAgentTest extends AbstractAgentTest<LysaAgent> {
    private Provider provider = new Provider();

    public LysaAgentTest() {
        super(LysaAgent.class);
    }

    @Test
    public void testUser1MobileBankId() throws Exception {
        testAgent(TestSSN.FH, null, CredentialsTypes.MOBILE_BANKID, true);
    }

    @Override
    protected Provider constructProvider() {
        return provider;
    }
}
