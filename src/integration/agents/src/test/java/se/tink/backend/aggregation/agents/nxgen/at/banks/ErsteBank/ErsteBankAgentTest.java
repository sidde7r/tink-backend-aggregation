package se.tink.backend.aggregation.agents.nxgen.at.banks.ErsteBank;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.UsernamePasswordArgumentEnum;

public class ErsteBankAgentTest {
    private final ArgumentManager<UsernamePasswordArgumentEnum> manager =
            new ArgumentManager<>(UsernamePasswordArgumentEnum.values());

    @Before
    public void setUp() throws Exception {
        manager.before();
    }

    @Test
    public void testLoginAndRefresh() throws Exception {
        new AgentIntegrationTest.Builder("at", "at-erstebank-password")
                .addCredentialField(
                        Field.Key.USERNAME, manager.get(UsernamePasswordArgumentEnum.USERNAME))
                .addCredentialField(
                        Field.Key.PASSWORD, manager.get(UsernamePasswordArgumentEnum.PASSWORD))
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(false)
                .build()
                .testRefresh();
    }

    @Test
    public void testLoginAndRefreshSidentity() throws Exception {
        new AgentIntegrationTest.Builder("at", "at-erstebank-thirdparty")
                .addCredentialField(
                        Field.Key.USERNAME, manager.get(UsernamePasswordArgumentEnum.USERNAME))
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(false)
                .build()
                .testRefresh();
    }

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }
}
