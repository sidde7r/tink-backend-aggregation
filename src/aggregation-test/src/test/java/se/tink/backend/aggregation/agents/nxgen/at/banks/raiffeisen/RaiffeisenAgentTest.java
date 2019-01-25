package se.tink.backend.aggregation.agents.nxgen.at.banks.raiffeisen;

import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

public class RaiffeisenAgentTest {
    @Test
    public void testLoginAndRefresh2() throws Exception {
        new AgentIntegrationTest.Builder("at", RaiffeisenTestConfig.PROVIDER_NAME2)
                .addCredentialField(Field.Key.USERNAME, RaiffeisenTestConfig.USERNAME2)
                .addCredentialField(Field.Key.PASSWORD, RaiffeisenTestConfig.PASSWORD2)
                .loadCredentialsBefore(true) // true ~ run python agent-test-server.py
                .saveCredentialsAfter(true)
                //.doLogout(true) // false is the default
                .transactionsToPrint(0) // 0 ~ no limit
                .build()
                .testRefresh();
    }

    @Test
    public void testLoginAndRefresh6() throws Exception {
        new AgentIntegrationTest.Builder("at", RaiffeisenTestConfig.PROVIDER_NAME6)
                .addCredentialField(Field.Key.USERNAME, RaiffeisenTestConfig.USERNAME6)
                .addCredentialField(Field.Key.PASSWORD, RaiffeisenTestConfig.PASSWORD6)
                .loadCredentialsBefore(true) // true ~ run python agent-test-server.py
                .saveCredentialsAfter(true)
                //.doLogout(true) // false is the default
                .transactionsToPrint(0) // 0 ~ no limit
                .build()
                .testRefresh();
    }
}
