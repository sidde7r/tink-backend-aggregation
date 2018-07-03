package se.tink.backend.aggregation.agents.banks.norwegian;

import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.rpc.CredentialsTypes;
import se.tink.backend.aggregation.rpc.Field;

public class NorwegianAgentTest {
    private final String USERNAME = "";

    @Test
    public void testLogin() throws Exception {
        new AgentIntegrationTest.Builder("se", "norwegian-bankid")
                .setCredentialType(CredentialsTypes.MOBILE_BANKID)
                .addCredentialField(Field.Key.USERNAME, USERNAME)
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(true)
                .build()
                .testRefresh();
    }
}
