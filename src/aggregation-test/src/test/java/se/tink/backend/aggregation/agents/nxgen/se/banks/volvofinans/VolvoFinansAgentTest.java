package se.tink.backend.aggregation.agents.nxgen.se.banks.volvofinans;

import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.rpc.Field;

@Ignore
public class VolvoFinansAgentTest {

    private final String USERNAME = "";

    @Test
    public void testLogin() throws Exception {
        new AgentIntegrationTest.Builder("se", "volvofinans-bankid")
                .addCredentialField(Field.Key.USERNAME, USERNAME)
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(false)
                .build()
                .testRefresh();
    }
}
