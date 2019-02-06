package se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

@Ignore
public class NordeaFIAgentTest {
    private static final String USERNAME = "";

    private AgentIntegrationTest.Builder builder;

    @Before
    public void setup() {
        builder = new AgentIntegrationTest.Builder("fi", "fi-nordea-thirdpartyapp")
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
