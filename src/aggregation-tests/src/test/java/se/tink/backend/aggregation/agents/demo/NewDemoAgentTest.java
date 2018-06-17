package se.tink.backend.aggregation.agents.demo;

import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.rpc.CredentialsTypes;
import se.tink.backend.aggregation.rpc.Field;
import se.tink.credentials.demo.DemoCredentials;

@Ignore
public class NewDemoAgentTest {
    private static final AgentIntegrationTest.Builder builder = new AgentIntegrationTest.Builder("se", "demo")
            .setCredentialType(CredentialsTypes.PASSWORD)
            .addCredentialField(Field.Key.PASSWORD, "demo")
            .loadCredentialsBefore(false)
            .saveCredentialsAfter(false);

    @Test
    public void testDemoSupplementalInfo() throws Exception {
        builder.addCredentialField(Field.Key.USERNAME, DemoCredentials.USER8.getUsername())
                .build()
                .testRefresh();
    }

    @Test
    public void testDemoSwe() throws Exception {
        builder.addCredentialField(Field.Key.USERNAME, DemoCredentials.USER4.getUsername())
                .build()
                .testRefresh();
    }
}
