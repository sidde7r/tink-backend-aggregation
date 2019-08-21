package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;

public class OpBankAgentTest {

    private final ArgumentManager<OpBankAgentTest.Arg> manager =
            new ArgumentManager<>(OpBankAgentTest.Arg.values());
    private AgentIntegrationTest.Builder builder;

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }

    @Before
    public void setup() {
        manager.before();

        builder =
                new AgentIntegrationTest.Builder("fi", "fi-opbank-openbanking")
                        .addCredentialField(Field.Key.USERNAME, manager.get(Arg.USERNAME))
                        .addCredentialField(Field.Key.PASSWORD, manager.get(Arg.PASSWORD))
                        .expectLoggedIn(false)
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false);
    }

    @Test
    public void testRefresh() throws Exception {
        builder.build().testRefresh();
    }

    private enum Arg {
        USERNAME,
        PASSWORD
    }
}
