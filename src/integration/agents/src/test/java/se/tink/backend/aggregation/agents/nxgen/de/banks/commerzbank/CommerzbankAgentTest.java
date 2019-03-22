package se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank;

import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

@Ignore
public class CommerzbankAgentTest {

    public static final String USERNAME = "";
    public static final String PASSWORD = "";

    @Test
    public void refresh() throws Exception {
        new AgentIntegrationTest.Builder("de", "de-commerzbank-password")
                .addCredentialField(Field.Key.USERNAME, USERNAME)
                .addCredentialField(Field.Key.PASSWORD, PASSWORD)
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(false)
                .doLogout(true)
                .build()
                .testRefresh();
    }
}
