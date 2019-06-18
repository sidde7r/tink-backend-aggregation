package se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

public class NordeaSeAgentTest {

    private AgentIntegrationTest.Builder builder;

    private final String SSN = "";

    @Before
    public void setup() {
        builder =
                new AgentIntegrationTest.Builder("SE", "se-nordea-oauth2")
                        .addCredentialField(Field.Key.USERNAME, SSN)
                        .expectLoggedIn(false)
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(true);
    }

    @Test
    public void testRefresh() throws Exception {
        builder.build().testRefresh();
    }
}
