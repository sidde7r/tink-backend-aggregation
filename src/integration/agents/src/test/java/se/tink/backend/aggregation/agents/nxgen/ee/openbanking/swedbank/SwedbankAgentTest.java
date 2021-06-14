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
        return new AgentIntegrationTest.Builder("ee", "ee-swedbank-smartid")
                .addCredentialField(Field.Key.USERNAME, manager.get(UsernameArgumentEnum.USERNAME))
                // random tmp id for test purposes
                .addCredentialField(Field.Key.NATIONAL_ID_NUMBER, "11111111111")
                .expectLoggedIn(false)
                .setFinancialInstitutionId("swedbank")
                .setAppId("tink")
                .loadCredentialsBefore(true)
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
