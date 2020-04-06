package se.tink.backend.aggregation.agents.creditcards.sebkort;

import org.junit.Test;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.agents.framework.legacy.AbstractAgentTest;

public class EurocardAgentTest extends AbstractAgentTest<SEBKortAgent> {
    public EurocardAgentTest() {
        super(SEBKortAgent.class);
    }

    private final String username1 = "198103224856";
    private final String password1 = "danielkj81";

    @Test
    public void testUser1Password() throws Exception {
        testAgent(username1, password1, CredentialsTypes.PASSWORD);
    }

    @Test
    public void testUser1MobileBankId() throws Exception {
        testAgent(username1, password1, CredentialsTypes.MOBILE_BANKID);
    }

    @Test
    public void testUser1Stability() throws Exception {
        for (int i = 0; i < 30; i++) {
            testAgent(username1, password1);
        }
    }

    @Test
    public void testUser1AuthenticationError() throws Exception {
        testAgentAuthenticationError("198103224856", "danielkj82");
    }

    @Override
    protected Provider constructProvider() {
        Provider provider = new Provider();

        provider.setPayload("ecse:0005");

        return provider;
    }
}
