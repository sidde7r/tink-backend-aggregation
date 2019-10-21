package se.tink.backend.aggregation.agents.nxgen.pt.banks.santander;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;

public class SantanderAgentTest {

    private enum Arg {
        USERNAME,
        PASSWORD
    }

    private final ArgumentManager<SantanderAgentTest.Arg> manager =
            new ArgumentManager<>(SantanderAgentTest.Arg.values());

    private AgentIntegrationTest.Builder builder;

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }

    @Before
    public void setup() {
        manager.before();

        builder =
                new AgentIntegrationTest.Builder("pt", "pt-santander-password")
                        .addCredentialField(
                                Field.Key.USERNAME, manager.get(SantanderAgentTest.Arg.USERNAME))
                        .addCredentialField(
                                Field.Key.PASSWORD, manager.get(SantanderAgentTest.Arg.PASSWORD))
                        .expectLoggedIn(true)
                        .saveCredentialsAfter(false)
                        .loadCredentialsBefore(false);
    }

    @Test
    public void testRefresh() throws Exception {
        builder.build().testRefresh();
    }
}
