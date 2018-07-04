package se.tink.backend.aggregation.agents.nxgen.de.banks.fidor;

import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.rpc.CredentialsTypes;
import se.tink.backend.aggregation.rpc.Field;

@Ignore
public class FidorAgentTest {

    private String USERNAME = "";
    private String PASSWORD = "";

    @Test
    public void testLogin() throws Exception {
        new AgentIntegrationTest.Builder("de", "de-fidor-password")
                .setCredentialType(CredentialsTypes.PASSWORD)
                .addCredentialField(Field.Key.USERNAME, USERNAME)
                .addCredentialField(Field.Key.PASSWORD, PASSWORD)
                .build()
                .testRefresh();
    }

}