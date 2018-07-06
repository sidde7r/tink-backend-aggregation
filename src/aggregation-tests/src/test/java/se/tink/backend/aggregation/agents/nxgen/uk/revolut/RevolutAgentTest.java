package se.tink.backend.aggregation.agents.nxgen.uk.revolut;

import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.rpc.CredentialsTypes;
import se.tink.backend.aggregation.rpc.Field;

@Ignore
public class RevolutAgentTest {

    private final String USERNAME = ""; // phone number 00{country code}{phone number}, eg. 0046701234567
    private final String PASSWORD = ""; // four digits, eg. 1234

    @Test
    public void testLoginRefresh() throws Exception {
        new AgentIntegrationTest.Builder("uk", "uk-revolut-password")
                .setCredentialType(CredentialsTypes.PASSWORD)
                .addCredentialField(Field.Key.USERNAME, USERNAME)
                .addCredentialField(Field.Key.PASSWORD, PASSWORD)
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(false)
                .build()
                .testRefresh();
    }
}