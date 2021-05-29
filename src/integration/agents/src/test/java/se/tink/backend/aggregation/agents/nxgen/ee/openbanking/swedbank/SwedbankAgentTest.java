package se.tink.backend.aggregation.agents.nxgen.ee.openbanking.swedbank;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.UsernameArgumentEnum;

public class SwedbankAgentTest {
    private final ArgumentManager<UsernameArgumentEnum> manager =
            new ArgumentManager<>(UsernameArgumentEnum.values());

    @Before
    public void setup() {
        manager.before();
    }

    private AgentIntegrationTest setupAgentTest() {
        return new AgentIntegrationTest.Builder("ee", "ee-swedbank-ob")
                .addCredentialField(Field.Key.USERNAME, manager.get(UsernameArgumentEnum.USERNAME))
                .expectLoggedIn(false)
                .setFinancialInstitutionId("swedbank")
                .setAppId("tink")
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(true)
                .build();
    }

    @Test
    public void testRefresh() throws Exception {
        setupAgentTest().testRefresh();
    }

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }
}
