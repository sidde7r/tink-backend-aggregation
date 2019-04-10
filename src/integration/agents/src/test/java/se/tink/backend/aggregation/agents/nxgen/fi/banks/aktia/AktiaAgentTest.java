package se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia;

import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

@Ignore
public class AktiaAgentTest {
    private static final String USERNAME = "USERNAME";
    private static final String PASSWORD = "PASSWORD";

    private final AgentIntegrationTest.Builder builder =
            new AgentIntegrationTest.Builder("fi", "fi-aktia-codecard")
                    .addCredentialField(Field.Key.USERNAME, USERNAME)
                    .addCredentialField(Field.Key.PASSWORD, PASSWORD)
                    .doLogout(false)
                    .expectLoggedIn(false)
                    .loadCredentialsBefore(true)
                    .saveCredentialsAfter(true);

    @Test
    public void testRefresh() throws Exception {
        builder.build().testRefresh();
    }
}
