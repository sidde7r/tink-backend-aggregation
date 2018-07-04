package se.tink.backend.aggregation.agents.nxgen.at.banks.bawagpsk;

import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.rpc.CredentialsTypes;
import se.tink.backend.aggregation.rpc.Field;

public class BawagPskAgentTest {
    private static final AgentIntegrationTest.Builder builder = new AgentIntegrationTest.Builder("at", "at-bawag-psk")
            .setCredentialType(CredentialsTypes.PASSWORD)
            .addCredentialField(Field.Key.PASSWORD, TestConfig.PASSWORD)
            .loadCredentialsBefore(false)
            .saveCredentialsAfter(false);

    @Test
    public void testLoginAndRefresh() throws Exception {
        builder.addCredentialField(Field.Key.USERNAME, TestConfig.USERNAME)
                .build()
                .testRefresh();
    }
}