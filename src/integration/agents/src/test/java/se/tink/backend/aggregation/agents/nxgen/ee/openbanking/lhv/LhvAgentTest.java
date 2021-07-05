package se.tink.backend.aggregation.agents.nxgen.ee.openbanking.lhv;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.SsnArgumentEnum;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.UsernameArgumentEnum;
import se.tink.libraries.credentials.service.RefreshableItem;

public class LhvAgentTest {

    private final ArgumentManager<SsnArgumentEnum> managerSsn =
            new ArgumentManager<>(SsnArgumentEnum.values());
    private final ArgumentManager<UsernameArgumentEnum> managerUsername =
            new ArgumentManager<>(UsernameArgumentEnum.values());

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }

    @Before
    public void setup() {
        managerSsn.before();
        managerUsername.before();
    }

    @Test
    public void testRefresh() throws Exception {
        new AgentIntegrationTest.Builder("ee", "ee-lhv-ob")
                .addCredentialField(
                        Key.USERNAME, managerUsername.get(UsernameArgumentEnum.USERNAME))
                .addCredentialField(Key.CORPORATE_ID, managerSsn.get(SsnArgumentEnum.SSN))
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(false)
                .setFinancialInstitutionId("lhv")
                .setAppId("tink")
                .expectLoggedIn(false)
                .addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray())
                .addRefreshableItems(RefreshableItem.IDENTITY_DATA)
                .build()
                .testRefresh();
    }
}
