package se.tink.backend.aggregation.agents.nxgen.ee.openbanking.swedbank;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.SsnArgumentEnum;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.UsernameArgumentEnum;

public class SwedbankEEAgentTest {
    private final ArgumentManager<UsernameArgumentEnum> manager =
            new ArgumentManager<>(UsernameArgumentEnum.values());
    private final ArgumentManager<SsnArgumentEnum> ssnManager =
            new ArgumentManager<>(SsnArgumentEnum.values());

    @Before
    public void setup() {
        manager.before();
        ssnManager.before();
    }

    private AgentIntegrationTest setupAgentTest() {
        return new AgentIntegrationTest.Builder("ee", "ee-swedbank-ob")
                .addCredentialField(Field.Key.USERNAME, manager.get(UsernameArgumentEnum.USERNAME))
                .addCredentialField(
                        Field.Key.NATIONAL_ID_NUMBER, ssnManager.get(SsnArgumentEnum.SSN))
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
