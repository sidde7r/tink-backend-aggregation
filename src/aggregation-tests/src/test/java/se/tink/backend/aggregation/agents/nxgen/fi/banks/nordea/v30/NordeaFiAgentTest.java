package se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v30;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.rpc.Field;

@Ignore
public class NordeaFiAgentTest {
    private static final String USERNAME = "NNNN";

    private AgentIntegrationTest.Builder builder;

    @Before
    public void setup() {
        builder = new AgentIntegrationTest.Builder("fi", "fi-nordea-codeslogin")
                .addCredentialField(Field.Key.USERNAME, USERNAME)
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(false)
                .doLogout(true);
    }

    @Test
    public void testRefresh() throws Exception {
        builder.build().testRefresh();
    }
}
