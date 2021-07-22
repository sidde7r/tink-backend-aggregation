package se.tink.backend.aggregation.agents.nxgen.ee.openbanking.seb;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.SsnArgumentEnum;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.UsernameArgumentEnum;
import se.tink.libraries.credentials.service.RefreshableItem;

public class SebEEAgentTest {

    private final ArgumentManager<SsnArgumentEnum> managerSsn =
            new ArgumentManager<>(SsnArgumentEnum.values());

    private final ArgumentManager<UsernameArgumentEnum> managerUserName =
            new ArgumentManager<>(UsernameArgumentEnum.values());

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }

    @Before
    public void setup() {
        managerUserName.before();
        managerSsn.before();
    }

    @Test
    public void testRefresh() throws Exception {
        new AgentIntegrationTest.Builder("ee", "ee-seb-ob")
                .addCredentialField(
                        Field.Key.USERNAME, managerUserName.get(UsernameArgumentEnum.USERNAME))
                .addCredentialField(Key.CORPORATE_ID, managerSsn.get(SsnArgumentEnum.SSN))
                .setFinancialInstitutionId("sebbaltic")
                .setAppId("tink")
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(false)
                .expectLoggedIn(false)
                .addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray())
                .addRefreshableItems(RefreshableItem.IDENTITY_DATA)
                .build()
                .testRefresh();
    }
}
