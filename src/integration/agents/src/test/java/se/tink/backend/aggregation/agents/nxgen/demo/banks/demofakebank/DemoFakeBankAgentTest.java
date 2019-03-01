package se.tink.backend.aggregation.agents.nxgen.demo.banks.demofakebank;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

public class DemoFakeBankAgentTest {
    private static final String USERNAME = "user";
    private static final String PASSWORD = "password";

    private AgentIntegrationTest.Builder builder;

    @Before
    public void setup() {
        builder =
                new AgentIntegrationTest.Builder("se", "se-test-demo-fake-bank")
                        .addCredentialField(Field.Key.USERNAME, USERNAME)
                        .addCredentialField(Field.Key.PASSWORD, PASSWORD)
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(true)
                        .doLogout(true);
    }

    @Test
    public void testRefresh() throws Exception {
        builder.build().testRefresh();
        builder.loadCredentialsBefore(true).build().testRefresh();
    }
}
