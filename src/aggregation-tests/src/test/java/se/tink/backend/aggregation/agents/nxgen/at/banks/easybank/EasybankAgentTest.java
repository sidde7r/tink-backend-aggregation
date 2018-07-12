package se.tink.backend.aggregation.agents.nxgen.at.banks.easybank;

import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.rpc.Field;

@Ignore
public class EasybankAgentTest {

    private final String USERNAME = "";
    private final String PASSWORD = "";

    @Test
    public void testLoginAndRefresh() throws Exception {
        new AgentIntegrationTest.Builder("at", "at-easybank-password")
                .addCredentialField(Field.Key.USERNAME, USERNAME)
                .addCredentialField(Field.Key.PASSWORD, PASSWORD)
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(false)
                .build()
                .testRefresh();
    }
}
