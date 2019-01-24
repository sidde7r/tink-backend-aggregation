package se.tink.backend.aggregation.agents.banks.seb;

import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.rpc.Field;

@Ignore
public class SEBAgentTest {
    public static final String USERNAME = "";

    @Test
    public void refresh() throws Exception {
        new AgentIntegrationTest.Builder("se", "seb-bankid")
                .addCredentialField(Field.Key.USERNAME, USERNAME)
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(true)
                .build()
                .testRefresh();
    }
}
