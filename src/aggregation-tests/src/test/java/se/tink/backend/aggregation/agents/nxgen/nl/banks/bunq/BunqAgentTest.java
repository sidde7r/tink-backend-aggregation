package se.tink.backend.aggregation.agents.nxgen.nl.banks.bunq;

import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.rpc.CredentialsTypes;
import se.tink.backend.aggregation.rpc.Field;

@Ignore
public class BunqAgentTest {
    private final String PASSWORD = "";

    @Test
    public void testLogin() throws Exception {
        new AgentIntegrationTest.Builder("nl", "nl-bunq-apikey")
                .setCredentialType(CredentialsTypes.PASSWORD)
                .addCredentialField(Field.Key.PASSWORD, PASSWORD)
                .loadCredentialsBefore(true)
                .saveCredentialsAfter(true)
                .build()
                .testRefresh();
    }
}
