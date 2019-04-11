package se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki;

import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

@Ignore
public class SpankkiAgentTest {
    private static final String USERNAME = "";
    private static final String PASSWORD = "";

    private AgentIntegrationTest.Builder builder() {
        return new AgentIntegrationTest.Builder("fi", "fi-spankki-codecard")
                .addCredentialField(Field.Key.USERNAME, USERNAME)
                .addCredentialField(Field.Key.PASSWORD, PASSWORD)
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(true);
    }

    @Test
    public void testRefresh() throws Exception {
        builder().build().testRefresh();
    }

    @Test
    public void testAutoRefresh() throws Exception {
        builder().loadCredentialsBefore(true).build().testRefresh();
    }
}
