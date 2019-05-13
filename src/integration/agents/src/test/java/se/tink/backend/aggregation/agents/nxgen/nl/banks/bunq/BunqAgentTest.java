package se.tink.backend.aggregation.agents.nxgen.nl.banks.bunq;

import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

//@Ignore
public class BunqAgentTest {
    private final String PASSWORD = "sandbox_fbb550093c35cc1066813125f0fce59271de1cdd1bed1a08fe23a5f5";

    @Test
    public void testLogin() throws Exception {
        new AgentIntegrationTest.Builder("nl", "nl-bunq-sandbox-apikey")
                .addCredentialField(Field.Key.PASSWORD, PASSWORD)
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(false)
                .build()
                .testRefresh();
    }
}
