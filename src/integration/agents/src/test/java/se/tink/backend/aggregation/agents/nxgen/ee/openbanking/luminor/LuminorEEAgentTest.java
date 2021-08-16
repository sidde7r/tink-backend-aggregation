package se.tink.backend.aggregation.agents.nxgen.ee.openbanking.luminor;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.UsernameArgumentEnum;
import se.tink.libraries.credentials.service.RefreshableItem;

public class LuminorEEAgentTest {

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
        new AgentIntegrationTest.Builder("ee", "ee-luminor-ob")
                .addCredentialField(Field.Key.USERNAME, "1111111")
                .addCredentialField(Field.Key.NATIONAL_ID_NUMBER, "22222222222")
                .expectLoggedIn(false)
                .loadCredentialsBefore(true)
                .saveCredentialsAfter(true)
                .setFinancialInstitutionId("luminor")
                .setAppId("tink")
                .addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray())
                .build()
                .testRefresh();
    }
}
