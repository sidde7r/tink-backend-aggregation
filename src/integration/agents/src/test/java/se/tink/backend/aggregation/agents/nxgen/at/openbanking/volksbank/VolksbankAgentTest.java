package se.tink.backend.aggregation.agents.nxgen.at.openbanking.volksbank;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;

public class VolksbankAgentTest {

    // TEST_PSU_ID => "100002"
    // TEST_PSU_ID_TYPE => "disposer-nr"

    private enum Arg {
        PSU_ID,
        PSU_ID_TYPE
    }

    private final ArgumentManager<Arg> manager = new ArgumentManager<>(Arg.values());

    private AgentIntegrationTest.Builder builder;

    @Before
    public void setup() {
        manager.before();

        builder =
                new AgentIntegrationTest.Builder("at", "at-volksbank-ob")
                        .addCredentialField(Key.LOGIN_INPUT, manager.get(Arg.PSU_ID))
                        .addCredentialField(Key.LOGIN_DESCRIPTION, manager.get(Arg.PSU_ID_TYPE))
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false)
                        .expectLoggedIn(false);
    }

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }

    @Test
    public void testRefresh() throws Exception {
        builder.build().testRefresh();
    }
}
