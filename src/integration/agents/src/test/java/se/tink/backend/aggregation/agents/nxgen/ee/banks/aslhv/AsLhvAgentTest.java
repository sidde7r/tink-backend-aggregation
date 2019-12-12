package se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.UsernamePasswordArgumentEnum;

public class AsLhvAgentTest {
    private final ArgumentManager<UsernamePasswordArgumentEnum> helper =
            new ArgumentManager<>(UsernamePasswordArgumentEnum.values());

    private final AgentIntegrationTest.Builder builder =
            new AgentIntegrationTest.Builder("ee", "ee-aslhv-password")
                    .loadCredentialsBefore(false)
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
    public void testLoginAndRefresh() throws Exception {
        builder.addCredentialField(
                        Field.Key.USERNAME, helper.get(UsernamePasswordArgumentEnum.USERNAME))
                .addCredentialField(
                        Field.Key.PASSWORD, helper.get(UsernamePasswordArgumentEnum.PASSWORD))
                .doLogout(true)
                .build()
                .testRefresh();
    }
}
