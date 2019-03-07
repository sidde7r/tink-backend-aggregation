package se.tink.backend.aggregation.agents.nxgen.se.openbanking.seb;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

@Ignore
public class SEBAgentTest {
    private final String TEST_USERNAME = "6101122640";
    private final String TEST_PASSWORD = "Doris";

    private AgentIntegrationTest.Builder builder;

    @Before
    public void setup() {
        builder = new AgentIntegrationTest.Builder("se", "se-SEB-password")
                .addCredentialField(Field.Key.USERNAME, TEST_USERNAME)
                .addCredentialField(Field.Key.PASSWORD, TEST_PASSWORD)
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(false);
    }

    @Test
    public void testRefresh() throws Exception {
        builder.build().testRefresh();
    }
}
