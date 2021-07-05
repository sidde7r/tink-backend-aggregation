package se.tink.backend.aggregation.agents.nxgen.lt.openbanking.luminor;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.UsernameArgumentEnum;

public class LuminorLTAgentTest {

    private final ArgumentManager<UsernameArgumentEnum> manager =
            new ArgumentManager<>(UsernameArgumentEnum.values());

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }

    @Before
    public void setup() {
        manager.before();
    }

    @Test
    public void testRefresh() throws Exception {
        new AgentIntegrationTest.Builder("lt", "lt-luminor-ob")
                .addCredentialField(Field.Key.USERNAME, "1111111")
                .addCredentialField(Field.Key.NATIONAL_ID_NUMBER, "22222222222")
                .expectLoggedIn(false)
                .loadCredentialsBefore(true)
                .saveCredentialsAfter(true)
                .setFinancialInstitutionId("luminor")
                .setAppId("tink")
                .build()
                .testRefresh();
    }
}
