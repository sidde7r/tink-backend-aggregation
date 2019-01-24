package se.tink.backend.aggregation.agents.nxgen.at.banks.bankaustria;

import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.rpc.Field;

@Ignore
public class BankAustriaAgentTest {
    private static final String USERNAME = "";
    private static final String PIN = "";

    private final AgentIntegrationTest.Builder builder =
            new AgentIntegrationTest.Builder("at", "at-bankaustria-password")
                    .addCredentialField(Field.Key.USERNAME, USERNAME)
                    .addCredentialField(Field.Key.PASSWORD, PIN)
                    .doLogout(true)
                    .loadCredentialsBefore(false)
                    .saveCredentialsAfter(false);

    @Test
    public void testRefresh() throws Exception {
        builder.build()
                .testRefresh();
    }

}
