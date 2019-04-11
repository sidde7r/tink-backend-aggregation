package se.tink.backend.aggregation.agents.demo;

import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.libraries.credentials.demo.DemoCredentials;

@Ignore
public class NewDemoAgentTest {

    private static AgentIntegrationTest.Builder builder() {
        return new AgentIntegrationTest.Builder("se", "demo")
                .addCredentialField(Field.Key.PASSWORD, "demo")
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(false);
    }

    @Test
    public void testDemoSupplementalInfo() throws Exception {
        builder()
                .addCredentialField(Field.Key.USERNAME, DemoCredentials.USER8.getUsername())
                .build()
                .testRefresh();
    }

    @Test
    public void testDemoSwe() throws Exception {
        builder()
                .addCredentialField(Field.Key.USERNAME, DemoCredentials.USER4.getUsername())
                .build()
                .testRefresh();
    }
}
