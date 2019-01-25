package se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea;

import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

@Ignore
public class NordeaDkAgentTest {
    private static final String USERNAME = "";
    private static final String PASSWORD = "";

    private AgentIntegrationTest.Builder builder() {
        return new AgentIntegrationTest.Builder("dk", "dk-nordea-nemid")
                .addCredentialField(Field.Key.USERNAME, USERNAME)
                .addCredentialField(Field.Key.PASSWORD, PASSWORD)
                .expectLoggedIn(false)
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(true);
    }

    @Test
    public void testRefresh() throws Exception {
        builder().build().testRefresh();
    }
}
