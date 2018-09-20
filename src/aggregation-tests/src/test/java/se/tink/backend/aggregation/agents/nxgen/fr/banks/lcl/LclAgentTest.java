package se.tink.backend.aggregation.agents.nxgen.fr.banks.lcl;

import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.rpc.Field;

@Ignore
public class LclAgentTest {
    private static final String USERNAME = "";
    private static final String PASSWORD = "";

    private static AgentIntegrationTest.Builder builder() {
        return new AgentIntegrationTest.Builder("fr", "fr-lcl-password")
                .loadCredentialsBefore(true)
                .saveCredentialsAfter(true);
    }

    @Test
    public void testRefresh() throws Exception {
        builder()
                .addCredentialField(Field.Key.USERNAME, USERNAME)
                .addCredentialField(Field.Key.PASSWORD, PASSWORD)
                .build()
                .testRefresh();
    }
}
