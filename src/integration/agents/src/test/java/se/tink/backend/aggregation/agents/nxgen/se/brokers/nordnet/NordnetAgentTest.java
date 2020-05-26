package se.tink.backend.aggregation.agents.nxgen.se.brokers.nordnet;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;

public class NordnetAgentTest {
    private final ArgumentManager<ArgumentManager.UsernameArgumentEnum> manager =
            new ArgumentManager<>(ArgumentManager.UsernameArgumentEnum.values());

    private AgentIntegrationTest.Builder builder;

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }

    @Before
    public void setup() {
        manager.before();
        builder =
                new AgentIntegrationTest.Builder("se", "nxgen-nordnet-bankid")
                        .addCredentialField(
                                Field.Key.USERNAME,
                                manager.get(ArgumentManager.UsernameArgumentEnum.USERNAME))
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(true);
    }

    @Test
    public void testRefresh() throws Exception {
        builder.build().testRefresh();
    }
}
