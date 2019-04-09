package se.tink.backend.aggregation.agents.banks.norwegian;

import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

@Ignore
public class NorwegianAgentTest {
    private final String USERNAME = "";

    @Test
    public void testLogin() throws Exception {
        new AgentIntegrationTest.Builder("se", "norwegian-bankid")
                .addCredentialField(Field.Key.USERNAME, USERNAME)
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(true)
                .build()
                .testRefresh();
    }
}
