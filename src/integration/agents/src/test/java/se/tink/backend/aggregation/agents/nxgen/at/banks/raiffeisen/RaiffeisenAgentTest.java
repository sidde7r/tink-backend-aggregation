package se.tink.backend.aggregation.agents.nxgen.at.banks.raiffeisen;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentHelper;

public class RaiffeisenAgentTest {
    private final ArgumentHelper helper = new ArgumentHelper("tink.username", "tink.password");

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
        ArgumentHelper.afterClass();
    }

    @Test
    public void testLoginAndRefresh2() throws Exception {
        builderRaff2
                .addCredentialField(Field.Key.USERNAME, helper.get("tink.username"))
                .addCredentialField(Field.Key.PASSWORD, helper.get("tink.password"))
                // .doLogout(true)
                .transactionsToPrint(0) // 0 ~ no limit
                .build()
                .testRefresh();
    }

    @Test
    public void testLoginAndRefresh6() throws Exception {
        builderRaff6
                .addCredentialField(Field.Key.USERNAME, helper.get("tink.username"))
                .addCredentialField(Field.Key.PASSWORD, helper.get("tink.password"))
                // .doLogout(true)
                .transactionsToPrint(0) // 0 ~ no limit
                .build()
                .testRefresh();
    }
}
