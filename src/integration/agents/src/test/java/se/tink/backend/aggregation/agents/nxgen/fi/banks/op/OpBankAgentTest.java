package se.tink.backend.aggregation.agents.nxgen.fi.banks.op;

import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

@Ignore
public class OpBankAgentTest {
    private static final String USERNAME = "";
    private static final String PASSWORD = "";

    private AgentIntegrationTest.Builder builder() {
        return new AgentIntegrationTest.Builder("fi", "fi-op-codecard")
                .addCredentialField(Field.Key.USERNAME, USERNAME)
                .addCredentialField(Field.Key.PASSWORD, PASSWORD)
                .expectLoggedIn(false)
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(true);
    }

    @Test
    public void testRefresh() throws Exception {
        builder().loadCredentialsBefore(false).saveCredentialsAfter(true).build().testRefresh();
    }

    @Test
    public void testAutoRefresh() throws Exception {
        builder().loadCredentialsBefore(true).saveCredentialsAfter(true).build().testRefresh();
    }
}
