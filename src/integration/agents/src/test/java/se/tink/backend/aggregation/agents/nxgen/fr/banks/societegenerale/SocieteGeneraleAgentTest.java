package se.tink.backend.aggregation.agents.nxgen.fr.banks.societegenerale;

import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

public class SocieteGeneraleAgentTest {

    private static final String USERNAME = ""; // 8 digits
    private static final String PASSWORD = ""; // 6 digits

    @Test
    public void testLoginRefresh() throws Exception {
        new AgentIntegrationTest.Builder(
                        SocieteGeneraleConstants.MARKET, SocieteGeneraleConstants.PROVIDER_NAME)
                .addCredentialField(Field.Key.USERNAME, USERNAME)
                .addCredentialField(Field.Key.PASSWORD, PASSWORD)
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(false)
                .build()
                .testRefresh();
    }
}
