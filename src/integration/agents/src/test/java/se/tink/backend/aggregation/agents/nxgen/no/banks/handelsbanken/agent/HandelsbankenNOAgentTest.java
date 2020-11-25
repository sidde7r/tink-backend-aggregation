package se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.agent;

import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

public class HandelsbankenNOAgentTest {
    private static final String USERNAME = "";
    private static final String MOBILENUMBER = "";

    @Test
    public void testRegisterAndRefresh() throws Exception {
        AgentIntegrationTest.Builder builder =
                new AgentIntegrationTest.Builder("no", "no-handelsbanken-bankid")
                        .addCredentialField(Field.Key.USERNAME, USERNAME)
                        .addCredentialField(Field.Key.MOBILENUMBER, MOBILENUMBER)
                        .loadCredentialsBefore(true)
                        .saveCredentialsAfter(true);

        builder.build().testRefresh();
    }
}
