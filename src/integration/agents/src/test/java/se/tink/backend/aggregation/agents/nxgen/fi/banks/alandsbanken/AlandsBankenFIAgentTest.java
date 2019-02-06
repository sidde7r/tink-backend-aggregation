package se.tink.backend.aggregation.agents.nxgen.fi.banks.alandsbanken;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

@Ignore
public class AlandsBankenFIAgentTest {
    private static final String USERNAME = "";
    private static final String PASSWORD = "";

    private AgentIntegrationTest.Builder builder;

    @Before
    public void setup() {
        builder = new AgentIntegrationTest.Builder("fi", "fi-alandsbanken-codecard")
                .addCredentialField(Field.Key.USERNAME, USERNAME)
                .addCredentialField(Field.Key.PASSWORD, PASSWORD)
                .loadCredentialsBefore(true)
                .saveCredentialsAfter(true);
    }

    @Test
    public void testRefresh() throws Exception {
        builder.build().testRefresh();
    }
}
