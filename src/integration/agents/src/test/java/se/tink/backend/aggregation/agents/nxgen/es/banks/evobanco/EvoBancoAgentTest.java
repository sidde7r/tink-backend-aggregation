package se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

@Ignore
public class EvoBancoAgentTest {
    private final String TEST_USERNAME = "CCCCC";
    private final String TEST_PASSWORD = "NNNNNN";

    private AgentIntegrationTest.Builder builder;

    @Before
    public void setup() {
        builder =
                new AgentIntegrationTest.Builder("es", "es-evobanco-password")
                        .addCredentialField(Field.Key.USERNAME, TEST_USERNAME)
                        .addCredentialField(Field.Key.PASSWORD, TEST_PASSWORD)
                        .loadCredentialsBefore(true)
                        .saveCredentialsAfter(true);
    }

    @Test
    public void testRefresh() throws Exception {

        builder.build().testRefresh();
    }
}
