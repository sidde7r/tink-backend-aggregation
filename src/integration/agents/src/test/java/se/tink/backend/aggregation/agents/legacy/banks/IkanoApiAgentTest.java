package se.tink.backend.aggregation.agents.banks;

import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

@Ignore
public class IkanoApiAgentTest {
    private final String USERNAME = "";

    @Test
    public void testLoginAndRefresh() throws Exception {
        AgentIntegrationTest.Builder builder = new AgentIntegrationTest.Builder("se", "preem-bankid")
                .addCredentialField(Field.Key.USERNAME, USERNAME)
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(true);

        builder.build()
                .testRefresh();
    }
}
