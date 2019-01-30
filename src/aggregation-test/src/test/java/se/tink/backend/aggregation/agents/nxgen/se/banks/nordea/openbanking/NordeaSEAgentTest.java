package se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.openbanking;

import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

@Ignore
public class NordeaSEAgentTest {
    private static String SANDBOX_SE_TEST_USER = "";

    @Test
    public void testRefresh() throws Exception {
        AgentIntegrationTest.Builder builder =
                new AgentIntegrationTest.Builder("se", "se-nordea-openbanking")
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(true)
                .addCredentialField(Field.Key.USERNAME, SANDBOX_SE_TEST_USER)
                .expectLoggedIn(false);

        builder.build().testRefresh();
    }
}
