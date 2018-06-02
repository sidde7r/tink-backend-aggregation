package se.tink.backend.aggregation.agents.brokers;

import org.junit.Test;
import se.tink.backend.aggregation.agents.AbstractAgentTest;
import se.tink.backend.aggregation.agents.brokers.avanza.AvanzaV2Agent;
import se.tink.backend.aggregation.rpc.Credentials;
import se.tink.backend.aggregation.rpc.CredentialsTypes;
import se.tink.backend.aggregation.rpc.Provider;
import se.tink.backend.common.utils.TestSSN;

public class AvanzaAgentTest extends AbstractAgentTest<AvanzaV2Agent> {
    private static final String AUTHENTICATION_SESSION_PAYLOAD = "authenticationSession";
    private Provider provider = new Provider();

    public AvanzaAgentTest() {
        super(AvanzaV2Agent.class);
    }

    @Test
    public void testUser1AuthenticatedSession() throws Exception {
        provider.setCredentialsType(CredentialsTypes.MOBILE_BANKID);
        Credentials c = createCredentials(TestSSN.FH, null, CredentialsTypes.PASSWORD);
        c.setSensitivePayload(AUTHENTICATION_SESSION_PAYLOAD, "0bcd89e1-f010-40fe-86bf-b072d6b1d1cd");
        testAgent(c, false);
    }

    @Test
    public void testUser1AuthenticationError() throws Exception {
        provider.setCredentialsType(CredentialsTypes.PASSWORD);
        testAgentAuthenticationError("fhedberg", "brown81ian");
    }

    @Test
    public void testUser1MobileBankId() throws Exception {
        provider.setCredentialsType(CredentialsTypes.MOBILE_BANKID);
        testAgent(TestSSN.DL, null, CredentialsTypes.MOBILE_BANKID, false);
    }

    @Test
    public void testUser1Password() throws Exception {
        provider.setCredentialsType(CredentialsTypes.PASSWORD);
        testAgent("fhedberg", "brown82ian", CredentialsTypes.PASSWORD, false);
    }

    @Test
    public void testUser2() throws Exception {
        provider.setCredentialsType(CredentialsTypes.PASSWORD);
        testAgent("2412724", "EkgT32", CredentialsTypes.PASSWORD, false);
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
