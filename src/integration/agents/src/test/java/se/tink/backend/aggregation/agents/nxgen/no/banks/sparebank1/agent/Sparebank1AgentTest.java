package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.agent;

import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

public class Sparebank1AgentTest {
    private final String USERNAME = "";
    private final String MOBILE_NUMBER = "";

    @Test
    public void testRegister() throws Exception {
        new AgentIntegrationTest.Builder("no", "no-sparebank1-sr-bank")
                .addCredentialField(Field.Key.USERNAME, USERNAME)
                .addCredentialField(Field.Key.MOBILENUMBER, MOBILE_NUMBER)
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(false)
                .expectLoggedIn(false)
                .build()
                .testRefresh();
    }

    @Test
    public void testLogin() throws Exception {
        new AgentIntegrationTest.Builder("no", "no-sparebank1-sr-bank")
                .addCredentialField(Field.Key.USERNAME, USERNAME)
                .addCredentialField(Field.Key.MOBILENUMBER, MOBILE_NUMBER)
                .loadCredentialsBefore(true)
                .saveCredentialsAfter(true)
                .expectLoggedIn(false)
                .build()
                .testRefresh();
    }
}
