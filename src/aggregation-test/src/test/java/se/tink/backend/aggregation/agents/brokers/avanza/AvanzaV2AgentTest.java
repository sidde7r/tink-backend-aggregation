package se.tink.backend.aggregation.agents.brokers.avanza;

import org.junit.Test;
import se.tink.backend.aggregation.agents.AbstractAgentTest;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.aggregation.rpc.Provider;
import se.tink.libraries.social.security.TestSSN;

public class AvanzaV2AgentTest extends AbstractAgentTest<AvanzaV2Agent> {
    private Provider provider = new Provider();

    public AvanzaV2AgentTest() {
        super(AvanzaV2Agent.class);
    }

    @Test
    public void testUser1MobileBankId() throws Exception {
        provider.setCredentialsType(CredentialsTypes.MOBILE_BANKID);
        testAgent(TestSSN.DL, null, CredentialsTypes.MOBILE_BANKID, false);
    }

    @Test
    public void testUser4() throws Exception {
        Credentials credentials = new Credentials();
        credentials.setUsername(TestSSN.EP);
        credentials.setType(CredentialsTypes.MOBILE_BANKID);

        testAgent(credentials, false);
    }

    @Test
    public void testPersistentLoggedIn() throws Exception {

        Credentials credentials = new Credentials();
        credentials.setUsername(TestSSN.EP);
        credentials.setType(CredentialsTypes.MOBILE_BANKID);

        testAgentPersistentLoggedIn(credentials);
    }

    @Override
    protected Provider constructProvider() {
        return provider;
    }
}
