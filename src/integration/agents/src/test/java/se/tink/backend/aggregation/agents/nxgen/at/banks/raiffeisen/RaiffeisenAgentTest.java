package se.tink.backend.aggregation.agents.nxgen.at.banks.raiffeisen;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;

public class RaiffeisenAgentTest {
    private enum Arg {
        USERNAME,
        PASSWORD,
    }

    private final ArgumentManager<Arg> helper = new ArgumentManager<>(Arg.values());

    private final AgentIntegrationTest.Builder builderRaff2 =
            new AgentIntegrationTest.Builder("at", "at-raiffeisen2-password")
                    .loadCredentialsBefore(false) // true ~ run python agent_test_server.py
                    .saveCredentialsAfter(false);

    private final AgentIntegrationTest.Builder builderRaff6 =
            new AgentIntegrationTest.Builder("at", "at-raiffeisen6-password")
                    .loadCredentialsBefore(false) // true ~ run python agent_test_server.py
                    .saveCredentialsAfter(false);

    @Before
    public void before() {
        helper.before();
    }

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }

    @Test
    public void testLoginAndRefresh2() throws Exception {
        builderRaff2
                .addCredentialField(Field.Key.USERNAME, helper.get(Arg.USERNAME))
                .addCredentialField(Field.Key.PASSWORD, helper.get(Arg.PASSWORD))
                // .doLogout(true)
                .transactionsToPrint(0) // 0 ~ no limit
                .build()
                .testRefresh();
    }

    @Test
    public void testLoginAndRefresh6() throws Exception {
        builderRaff6
                .addCredentialField(Field.Key.USERNAME, helper.get(Arg.USERNAME))
                .addCredentialField(Field.Key.PASSWORD, helper.get(Arg.PASSWORD))
                // .doLogout(true)
                .transactionsToPrint(0) // 0 ~ no limit
                .build()
                .testRefresh();
    }
}
