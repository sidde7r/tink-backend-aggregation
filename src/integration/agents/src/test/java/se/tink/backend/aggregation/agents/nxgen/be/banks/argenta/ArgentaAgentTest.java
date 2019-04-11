package se.tink.backend.aggregation.agents.nxgen.be.banks.argenta;

import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

@Ignore
public class ArgentaAgentTest {
    private static final String USERNAME = "";

    private final AgentIntegrationTest.Builder builder =
            new AgentIntegrationTest.Builder("be", "be-argenta-cardreader")
                    .addCredentialField(Field.Key.USERNAME, USERNAME)
                    .loadCredentialsBefore(true)
                    .saveCredentialsAfter(true);

    // You will need card/ Argenta digipass and the service for supplementary information
    // Argenta allows a maximum of 10 devices, please remove not used devices by web interface after
    // testing
    @Test
    public void testRefresh() throws Exception {
        builder.build().testRefresh();
    }
}
