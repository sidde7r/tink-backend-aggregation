package se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank;

import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

@Ignore
public class ImaginBankAgentTest {
    private final String USERNAME = "";
    private final String PASSWORD = "";

    @Test
    public void testRefresh() throws Exception {
        new AgentIntegrationTest.Builder("es", "es-imaginbank-password")
                .addCredentialField(Field.Key.USERNAME, USERNAME)
                .addCredentialField(Field.Key.PASSWORD, PASSWORD)
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(true)
                .build()
                .testRefresh();
    }
}
